package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.AuditEventDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.AuditEventMongoRepository;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.AuditEventMapper;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventRepositoryAdapterTest {

    @Mock
    private AuditEventMongoRepository mongoRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private AuditEventMapper mapper;

    @InjectMocks
    private AuditEventRepositoryAdapter adapter;

    @Test
    void saveMapsDomainToDocumentAndBack() {
        AuditEvent event = TestFixtures.auditEvent();
        AuditEventDocument document = TestFixtures.auditEventDocument();
        when(mapper.toDocument(event)).thenReturn(document);
        when(mongoRepository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(event);

        AuditEvent result = adapter.save(event);

        assertThat(result).isSameAs(event);
        verify(mongoRepository).save(document);
    }

    @Test
    void findByFiltersBuildsQueryWithAllFilters() {
        LocalDateTime start = LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, Month.JANUARY, 31, 23, 59);
        AuditEventDocument document = TestFixtures.auditEventDocument();
        AuditEvent event = TestFixtures.auditEvent();
        when(mongoTemplate.find(org.mockito.ArgumentMatchers.any(Query.class), eq(AuditEventDocument.class)))
                .thenReturn(List.of(document));
        when(mapper.toDomain(document)).thenReturn(event);

        List<AuditEvent> result = adapter.findByFilters(start, end, AuditActionType.USER_LOGIN, TestFixtures.USER_ID);

        assertThat(result).containsExactly(event);
        Query query = captureQuery();
        Document queryObject = query.getQueryObject();
        assertThat(queryObject.get("actionType")).isEqualTo(AuditActionType.USER_LOGIN);
        assertThat(queryObject.get("userId")).isEqualTo(TestFixtures.USER_ID);
        assertThat((Document) queryObject.get("timestamp"))
                .containsEntry("$gte", start)
                .containsEntry("$lte", end);
    }

    @Test
    void findByFiltersBuildsQueryWithOnlyStartDate() {
        LocalDateTime start = LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0);
        when(mongoTemplate.find(org.mockito.ArgumentMatchers.any(Query.class), eq(AuditEventDocument.class)))
                .thenReturn(List.of());

        adapter.findByFilters(start, null, null, null);

        Document timestamp = (Document) captureQuery().getQueryObject().get("timestamp");
        assertThat(timestamp).containsEntry("$gte", start).doesNotContainKey("$lte");
    }

    @Test
    void findByFiltersBuildsQueryWithOnlyEndDate() {
        LocalDateTime end = LocalDateTime.of(2026, Month.JANUARY, 31, 23, 59);
        when(mongoTemplate.find(org.mockito.ArgumentMatchers.any(Query.class), eq(AuditEventDocument.class)))
                .thenReturn(List.of());

        adapter.findByFilters(null, end, null, null);

        Document timestamp = (Document) captureQuery().getQueryObject().get("timestamp");
        assertThat(timestamp).containsEntry("$lte", end).doesNotContainKey("$gte");
    }

    @Test
    void findByFiltersWithNoFiltersAllowsEmptyFilterQuery() {
        when(mongoTemplate.find(org.mockito.ArgumentMatchers.any(Query.class), eq(AuditEventDocument.class)))
                .thenReturn(List.of());

        adapter.findByFilters(null, null, null, null);

        assertThat(captureQuery().getQueryObject()).isEmpty();
    }

    private Query captureQuery() {
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(AuditEventDocument.class));
        return queryCaptor.getValue();
    }
}
