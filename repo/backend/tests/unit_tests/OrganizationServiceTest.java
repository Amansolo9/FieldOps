package unit_tests;

import com.eaglepoint.storehub.aspect.DataScopeContext;
import com.eaglepoint.storehub.dto.OrganizationDto;
import com.eaglepoint.storehub.entity.Organization;
import com.eaglepoint.storehub.enums.OrgLevel;
import com.eaglepoint.storehub.repository.OrganizationRepository;
import com.eaglepoint.storehub.service.OrganizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    @AfterEach
    void cleanup() { DataScopeContext.clear(); }

    @Test @DisplayName("create persists organization")
    void create_persists() {
        OrganizationDto dto = new OrganizationDto();
        dto.setName("New Site");
        dto.setLevel(OrgLevel.SITE);

        when(organizationRepository.save(any())).thenAnswer(inv -> {
            Organization org = inv.getArgument(0);
            org.setId(1L);
            return org;
        });

        OrganizationDto result = organizationService.create(dto);
        assertNotNull(result);
        assertEquals("New Site", result.getName());
    }

    @Test @DisplayName("create with parent links to parent")
    void create_withParent_links() {
        Organization parent = Organization.builder().id(1L).name("Enterprise").level(OrgLevel.ENTERPRISE).build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(organizationRepository.save(any())).thenAnswer(inv -> {
            Organization org = inv.getArgument(0);
            org.setId(2L);
            return org;
        });

        OrganizationDto dto = new OrganizationDto();
        dto.setName("Child Site");
        dto.setLevel(OrgLevel.SITE);
        dto.setParentId(1L);

        OrganizationDto result = organizationService.create(dto);
        assertEquals(1L, result.getParentId());
    }

    @Test @DisplayName("findByLevel returns filtered list")
    void findByLevel_returnsList() {
        DataScopeContext.set(null); // admin unrestricted
        when(organizationRepository.findByLevel(OrgLevel.SITE)).thenReturn(List.of(
                Organization.builder().id(1L).name("S1").level(OrgLevel.SITE).build()));

        List<OrganizationDto> result = organizationService.findByLevel(OrgLevel.SITE);
        assertEquals(1, result.size());
    }

    @Test @DisplayName("findChildren returns children of parent")
    void findChildren_returnsChildren() {
        DataScopeContext.set(null); // admin unrestricted
        when(organizationRepository.findByParentId(1L)).thenReturn(List.of(
                Organization.builder().id(2L).name("Team A").level(OrgLevel.TEAM).build()));

        List<OrganizationDto> result = organizationService.findChildren(1L);
        assertEquals(1, result.size());
        assertEquals("Team A", result.get(0).getName());
    }
}
