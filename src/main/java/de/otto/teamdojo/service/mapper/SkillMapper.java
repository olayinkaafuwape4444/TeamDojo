package de.otto.teamdojo.service.mapper;

import de.otto.teamdojo.domain.Skill;
import de.otto.teamdojo.service.dto.SkillDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity Skill and its DTO SkillDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface SkillMapper extends EntityMapper<SkillDTO, Skill> {


    @Mapping(target = "teams", ignore = true)
    @Mapping(target = "badges", ignore = true)
    @Mapping(target = "levels", ignore = true)
    Skill toEntity(SkillDTO skillDTO);

    default Skill fromId(Long id) {
        if (id == null) {
            return null;
        }
        Skill skill = new Skill();
        skill.setId(id);
        return skill;
    }
}
