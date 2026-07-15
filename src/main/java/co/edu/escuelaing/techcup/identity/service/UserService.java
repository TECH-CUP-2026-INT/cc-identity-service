package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.AuditEventType;
import co.edu.escuelaing.techcup.identity.document.AuditResult;
import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    /**
     * SCRUM-61: Inhabilitar usuario.
     * Marca la cuenta de un usuario como inactiva (enabled = false).
     * Solo puede ser ejecutado por un ADMIN u ORGANIZER (validado en el controller).
     * No se permite inhabilitar cuentas con rol ADMIN.
     * No se permite inhabilitar un usuario que ya está deshabilitado.
     *
     * @param userId identificador UUID del usuario a inhabilitar
     * @throws BusinessException si el usuario no existe, es ADMIN, o ya está deshabilitado
     */
    @Transactional
    public void disableUser(UUID userId) {
        UserDocument user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (user.getRole() == UserDocument.Role.ADMIN) {
            throw new BusinessException("Admin accounts cannot be disabled");
        }

        if (!user.isEnabled()) {
            throw new BusinessException("User is already disabled");
        }

        user.setEnabled(false);
        userRepository.save(user);
    }
}
