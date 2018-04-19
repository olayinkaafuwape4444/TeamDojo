package de.otto.dojo.web.rest;

import de.otto.dojo.DojoApp;

import de.otto.dojo.domain.TeamSkill;
import de.otto.dojo.repository.TeamSkillRepository;
import de.otto.dojo.service.TeamSkillService;
import de.otto.dojo.web.rest.errors.ExceptionTranslator;
import de.otto.dojo.service.dto.TeamSkillCriteria;
import de.otto.dojo.service.TeamSkillQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

import static de.otto.dojo.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TeamSkillResource REST controller.
 *
 * @see TeamSkillResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DojoApp.class)
public class TeamSkillResourceIntTest {

    private static final Instant DEFAULT_ACHIEVED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ACHIEVED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_VERIFIED = false;
    private static final Boolean UPDATED_VERIFIED = true;

    private static final String DEFAULT_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_NOTE = "BBBBBBBBBB";

    @Autowired
    private TeamSkillRepository teamSkillRepository;


    

    @Autowired
    private TeamSkillService teamSkillService;

    @Autowired
    private TeamSkillQueryService teamSkillQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restTeamSkillMockMvc;

    private TeamSkill teamSkill;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final TeamSkillResource teamSkillResource = new TeamSkillResource(teamSkillService, teamSkillQueryService);
        this.restTeamSkillMockMvc = MockMvcBuilders.standaloneSetup(teamSkillResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TeamSkill createEntity(EntityManager em) {
        TeamSkill teamSkill = new TeamSkill()
            .achievedAt(DEFAULT_ACHIEVED_AT)
            .verified(DEFAULT_VERIFIED)
            .note(DEFAULT_NOTE);
        return teamSkill;
    }

    @Before
    public void initTest() {
        teamSkill = createEntity(em);
    }

    @Test
    @Transactional
    public void createTeamSkill() throws Exception {
        int databaseSizeBeforeCreate = teamSkillRepository.findAll().size();

        // Create the TeamSkill
        restTeamSkillMockMvc.perform(post("/api/team-skills")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(teamSkill)))
            .andExpect(status().isCreated());

        // Validate the TeamSkill in the database
        List<TeamSkill> teamSkillList = teamSkillRepository.findAll();
        assertThat(teamSkillList).hasSize(databaseSizeBeforeCreate + 1);
        TeamSkill testTeamSkill = teamSkillList.get(teamSkillList.size() - 1);
        assertThat(testTeamSkill.getAchievedAt()).isEqualTo(DEFAULT_ACHIEVED_AT);
        assertThat(testTeamSkill.isVerified()).isEqualTo(DEFAULT_VERIFIED);
        assertThat(testTeamSkill.getNote()).isEqualTo(DEFAULT_NOTE);
    }

    @Test
    @Transactional
    public void createTeamSkillWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = teamSkillRepository.findAll().size();

        // Create the TeamSkill with an existing ID
        teamSkill.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTeamSkillMockMvc.perform(post("/api/team-skills")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(teamSkill)))
            .andExpect(status().isBadRequest());

        // Validate the TeamSkill in the database
        List<TeamSkill> teamSkillList = teamSkillRepository.findAll();
        assertThat(teamSkillList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllTeamSkills() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList
        restTeamSkillMockMvc.perform(get("/api/team-skills?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(teamSkill.getId().intValue())))
            .andExpect(jsonPath("$.[*].achievedAt").value(hasItem(DEFAULT_ACHIEVED_AT.toString())))
            .andExpect(jsonPath("$.[*].verified").value(hasItem(DEFAULT_VERIFIED.booleanValue())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE.toString())));
    }
    

    @Test
    @Transactional
    public void getTeamSkill() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get the teamSkill
        restTeamSkillMockMvc.perform(get("/api/team-skills/{id}", teamSkill.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(teamSkill.getId().intValue()))
            .andExpect(jsonPath("$.achievedAt").value(DEFAULT_ACHIEVED_AT.toString()))
            .andExpect(jsonPath("$.verified").value(DEFAULT_VERIFIED.booleanValue()))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE.toString()));
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByAchievedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where achievedAt equals to DEFAULT_ACHIEVED_AT
        defaultTeamSkillShouldBeFound("achievedAt.equals=" + DEFAULT_ACHIEVED_AT);

        // Get all the teamSkillList where achievedAt equals to UPDATED_ACHIEVED_AT
        defaultTeamSkillShouldNotBeFound("achievedAt.equals=" + UPDATED_ACHIEVED_AT);
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByAchievedAtIsInShouldWork() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where achievedAt in DEFAULT_ACHIEVED_AT or UPDATED_ACHIEVED_AT
        defaultTeamSkillShouldBeFound("achievedAt.in=" + DEFAULT_ACHIEVED_AT + "," + UPDATED_ACHIEVED_AT);

        // Get all the teamSkillList where achievedAt equals to UPDATED_ACHIEVED_AT
        defaultTeamSkillShouldNotBeFound("achievedAt.in=" + UPDATED_ACHIEVED_AT);
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByAchievedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where achievedAt is not null
        defaultTeamSkillShouldBeFound("achievedAt.specified=true");

        // Get all the teamSkillList where achievedAt is null
        defaultTeamSkillShouldNotBeFound("achievedAt.specified=false");
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByVerifiedIsEqualToSomething() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where verified equals to DEFAULT_VERIFIED
        defaultTeamSkillShouldBeFound("verified.equals=" + DEFAULT_VERIFIED);

        // Get all the teamSkillList where verified equals to UPDATED_VERIFIED
        defaultTeamSkillShouldNotBeFound("verified.equals=" + UPDATED_VERIFIED);
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByVerifiedIsInShouldWork() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where verified in DEFAULT_VERIFIED or UPDATED_VERIFIED
        defaultTeamSkillShouldBeFound("verified.in=" + DEFAULT_VERIFIED + "," + UPDATED_VERIFIED);

        // Get all the teamSkillList where verified equals to UPDATED_VERIFIED
        defaultTeamSkillShouldNotBeFound("verified.in=" + UPDATED_VERIFIED);
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByVerifiedIsNullOrNotNull() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where verified is not null
        defaultTeamSkillShouldBeFound("verified.specified=true");

        // Get all the teamSkillList where verified is null
        defaultTeamSkillShouldNotBeFound("verified.specified=false");
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByNoteIsEqualToSomething() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where note equals to DEFAULT_NOTE
        defaultTeamSkillShouldBeFound("note.equals=" + DEFAULT_NOTE);

        // Get all the teamSkillList where note equals to UPDATED_NOTE
        defaultTeamSkillShouldNotBeFound("note.equals=" + UPDATED_NOTE);
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByNoteIsInShouldWork() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where note in DEFAULT_NOTE or UPDATED_NOTE
        defaultTeamSkillShouldBeFound("note.in=" + DEFAULT_NOTE + "," + UPDATED_NOTE);

        // Get all the teamSkillList where note equals to UPDATED_NOTE
        defaultTeamSkillShouldNotBeFound("note.in=" + UPDATED_NOTE);
    }

    @Test
    @Transactional
    public void getAllTeamSkillsByNoteIsNullOrNotNull() throws Exception {
        // Initialize the database
        teamSkillRepository.saveAndFlush(teamSkill);

        // Get all the teamSkillList where note is not null
        defaultTeamSkillShouldBeFound("note.specified=true");

        // Get all the teamSkillList where note is null
        defaultTeamSkillShouldNotBeFound("note.specified=false");
    }
    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultTeamSkillShouldBeFound(String filter) throws Exception {
        restTeamSkillMockMvc.perform(get("/api/team-skills?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(teamSkill.getId().intValue())))
            .andExpect(jsonPath("$.[*].achievedAt").value(hasItem(DEFAULT_ACHIEVED_AT.toString())))
            .andExpect(jsonPath("$.[*].verified").value(hasItem(DEFAULT_VERIFIED.booleanValue())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultTeamSkillShouldNotBeFound(String filter) throws Exception {
        restTeamSkillMockMvc.perform(get("/api/team-skills?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    @Transactional
    public void getNonExistingTeamSkill() throws Exception {
        // Get the teamSkill
        restTeamSkillMockMvc.perform(get("/api/team-skills/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTeamSkill() throws Exception {
        // Initialize the database
        teamSkillService.save(teamSkill);

        int databaseSizeBeforeUpdate = teamSkillRepository.findAll().size();

        // Update the teamSkill
        TeamSkill updatedTeamSkill = teamSkillRepository.findById(teamSkill.getId()).get();
        // Disconnect from session so that the updates on updatedTeamSkill are not directly saved in db
        em.detach(updatedTeamSkill);
        updatedTeamSkill
            .achievedAt(UPDATED_ACHIEVED_AT)
            .verified(UPDATED_VERIFIED)
            .note(UPDATED_NOTE);

        restTeamSkillMockMvc.perform(put("/api/team-skills")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedTeamSkill)))
            .andExpect(status().isOk());

        // Validate the TeamSkill in the database
        List<TeamSkill> teamSkillList = teamSkillRepository.findAll();
        assertThat(teamSkillList).hasSize(databaseSizeBeforeUpdate);
        TeamSkill testTeamSkill = teamSkillList.get(teamSkillList.size() - 1);
        assertThat(testTeamSkill.getAchievedAt()).isEqualTo(UPDATED_ACHIEVED_AT);
        assertThat(testTeamSkill.isVerified()).isEqualTo(UPDATED_VERIFIED);
        assertThat(testTeamSkill.getNote()).isEqualTo(UPDATED_NOTE);
    }

    @Test
    @Transactional
    public void updateNonExistingTeamSkill() throws Exception {
        int databaseSizeBeforeUpdate = teamSkillRepository.findAll().size();

        // Create the TeamSkill

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restTeamSkillMockMvc.perform(put("/api/team-skills")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(teamSkill)))
            .andExpect(status().isCreated());

        // Validate the TeamSkill in the database
        List<TeamSkill> teamSkillList = teamSkillRepository.findAll();
        assertThat(teamSkillList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteTeamSkill() throws Exception {
        // Initialize the database
        teamSkillService.save(teamSkill);

        int databaseSizeBeforeDelete = teamSkillRepository.findAll().size();

        // Get the teamSkill
        restTeamSkillMockMvc.perform(delete("/api/team-skills/{id}", teamSkill.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<TeamSkill> teamSkillList = teamSkillRepository.findAll();
        assertThat(teamSkillList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TeamSkill.class);
        TeamSkill teamSkill1 = new TeamSkill();
        teamSkill1.setId(1L);
        TeamSkill teamSkill2 = new TeamSkill();
        teamSkill2.setId(teamSkill1.getId());
        assertThat(teamSkill1).isEqualTo(teamSkill2);
        teamSkill2.setId(2L);
        assertThat(teamSkill1).isNotEqualTo(teamSkill2);
        teamSkill1.setId(null);
        assertThat(teamSkill1).isNotEqualTo(teamSkill2);
    }
}
