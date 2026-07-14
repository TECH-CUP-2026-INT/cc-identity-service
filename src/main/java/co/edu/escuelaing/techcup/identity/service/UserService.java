package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.AuditEventType;
import co.edu.escuelaing.techcup.identity.document.AuditResult;
import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final RefereeValidator refereeValidator;
    private final TemporaryPasswordGenerator passwordGenerator;
    private final FullNameSplitter nameSplitter;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        OtpService otpService,
                        EmailService emailService,
                        RefereeValidator refereeValidator,
                        TemporaryPasswordGenerator passwordGenerator,
                        FullNameSplitter nameSplitter,
                        AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.emailService = emailService;
        this.refereeValidator = refereeValidator;
        this.passwordGenerator = passwordGenerator;
        this.nameSplitter = nameSplitter;
        this.auditService = auditService;
    }

    @Transactional
    public void createReferee(RefereeRequestDTO dto) {
        refereeValidator.validate(dto);

        String tempPassword = passwordGenerator.generate();
        String[] names = nameSplitter.split(dto.fullName());

        UserDocument referee = buildRefereeEntity(dto, names, tempPassword);
        userRepository.save(referee);

        notifyReferee(referee, tempPassword);

        auditService.record(AuditEventType.REFEREE_CREATED, AuditResult.SUCCESS,
                referee.getId(), referee.getEmail(), "Referee account created", null);
    }

    private UserDocument buildRefereeEntity(RefereeRequestDTO dto, String[] names, String tempPassword) {
        return UserDocument.builder()
                .firstName(names[0])
                .lastName(names[1])
                .dateOfBirth(dto.dateOfBirth())
                .idType(dto.idType())
                .idNumber(dto.idNumber())
                .email(dto.email())
                .role(UserDocument.Role.REFEREE)
                .enabled(true)
                .password(passwordEncoder.encode(tempPassword))
                .build();
    }

    private void notifyReferee(UserDocument referee, String tempPassword) {
        emailService.sendRefereeCredentials(referee.getEmail(), tempPassword);
        otpService.generateAndSend(referee);
    }
}