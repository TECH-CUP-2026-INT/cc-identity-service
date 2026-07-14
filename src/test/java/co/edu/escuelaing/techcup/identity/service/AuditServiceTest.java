package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.AuditEventDocument;
import co.edu.escuelaing.techcup.identity.document.AuditEventType;
import co.edu.escuelaing.techcup.identity.document.AuditResult;
import co.edu.escuelaing.techcup.identity.dto.AuditEventResponse;
import co.edu.escuelaing.techcup.identity.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(
                auditEventRepository,
                mongoTemplate
        );
    }

    @Test
    void record_savesAuditEvent() {
        UUID userId = UUID.randomUUID();

        auditService.record(
                AuditEventType.LOGIN_SUCCESS,
                AuditResult.SUCCESS,
                userId,
                "user@example.com",
                "Successful login",
                "Login completed correctly"
        );

        ArgumentCaptor<AuditEventDocument> captor =
                ArgumentCaptor.forClass(AuditEventDocument.class);

        verify(auditEventRepository).save(captor.capture());

        AuditEventDocument event = captor.getValue();

        assertNotNull(event.getId());
        assertNotNull(event.getTimestamp());
        assertEquals(userId, event.getUserId());
        assertEquals("user@example.com", event.getActorEmail());
        assertEquals(AuditEventType.LOGIN_SUCCESS, event.getEventType());
        assertEquals(AuditResult.SUCCESS, event.getResult());
        assertEquals("Successful login", event.getDescription());
        assertEquals("Login completed correctly", event.getDetail());
    }

    @Test
    void record_repositoryFails_doesNotThrow() {
        doThrow(new RuntimeException("Mongo unavailable"))
                .when(auditEventRepository)
                .save(any(AuditEventDocument.class));

        assertDoesNotThrow(() -> auditService.record(
                AuditEventType.LOGIN_FAILED,
                AuditResult.FAILURE,
                UUID.randomUUID(),
                "user@example.com",
                "Failed login",
                "Invalid credentials"
        ));
    }

    @Test
    void findEvents_withoutFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        AuditEventDocument event = createAuditEvent();

        when(mongoTemplate.count(
                any(Query.class),
                eq(AuditEventDocument.class)
        )).thenReturn(1L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(AuditEventDocument.class)
        )).thenReturn(List.of(event));

        Page<AuditEventResponse> result = auditService.findEvents(
                null,
                null,
                null,
                null,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(mongoTemplate).count(
                any(Query.class),
                eq(AuditEventDocument.class)
        );

        verify(mongoTemplate).find(
                any(Query.class),
                eq(AuditEventDocument.class)
        );
    }

    @Test
    void findEvents_withAllFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 5);
        UUID userId = UUID.randomUUID();

        LocalDateTime startDate =
                LocalDateTime.now().minusDays(1);

        LocalDateTime endDate =
                LocalDateTime.now().plusDays(1);

        AuditEventDocument event = createAuditEvent();
        event.setUserId(userId);

        when(mongoTemplate.count(
                any(Query.class),
                eq(AuditEventDocument.class)
        )).thenReturn(1L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(AuditEventDocument.class)
        )).thenReturn(List.of(event));

        Page<AuditEventResponse> result = auditService.findEvents(
                startDate,
                endDate,
                AuditEventType.LOGIN_SUCCESS,
                userId,
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findEvents_withOnlyStartDate_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(
                any(Query.class),
                eq(AuditEventDocument.class)
        )).thenReturn(0L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(AuditEventDocument.class)
        )).thenReturn(List.of());

        Page<AuditEventResponse> result = auditService.findEvents(
                LocalDateTime.now().minusHours(1),
                null,
                null,
                null,
                pageable
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void findEvents_withOnlyEndDate_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(
                any(Query.class),
                eq(AuditEventDocument.class)
        )).thenReturn(0L);

        when(mongoTemplate.find(
                any(Query.class),
                eq(AuditEventDocument.class)
        )).thenReturn(List.of());

        Page<AuditEventResponse> result = auditService.findEvents(
                null,
                LocalDateTime.now(),
                null,
                null,
                pageable
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void findEvents_startDateAfterEndDate_throwsException() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.minusDays(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> auditService.findEvents(
                        startDate,
                        endDate,
                        null,
                        null,
                        PageRequest.of(0, 10)
                )
        );

        assertEquals(
                "startDate must not be after endDate",
                exception.getMessage()
        );

        verifyNoInteractions(mongoTemplate);
    }

    private AuditEventDocument createAuditEvent() {
        return AuditEventDocument.builder()
                .id(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .eventType(AuditEventType.LOGIN_SUCCESS)
                .result(AuditResult.SUCCESS)
                .userId(UUID.randomUUID())
                .actorEmail("user@example.com")
                .description("Successful login")
                .detail("Login completed correctly")
                .build();
    }
}