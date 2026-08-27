package com.walkin.service.impl;

import com.walkin.entity.InterviewRound;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.InterviewRoundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewRoundServiceImplTests {
    @Mock InterviewRoundRepository repository;
    @InjectMocks InterviewRoundServiceImpl service;
    @Test void createAndListDelegateToRepository() {
        InterviewRound round = round("Technical"); when(repository.save(round)).thenReturn(round);
        assertSame(round, service.createInterviewRound(round)); when(repository.findAll()).thenReturn(List.of(round));
        assertEquals(List.of(round), service.getAllInterviewRounds());
    }
    @Test void missingRoundThrows() {
        when(repository.findById(4)).thenReturn(Optional.empty());
        assertEquals("Interview round not found with ID: 4", assertThrows(ResourceNotFoundException.class,
                () -> service.getInterviewRoundById(4)).getMessage());
    }
    @Test void updateAndDeleteUseExistingEntity() {
        InterviewRound existing=round("Old"), update=round("New"); when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing); assertSame(existing, service.updateInterviewRound(1, update));
        assertEquals("New", existing.getRoundName()); service.deleteInterviewRound(1); verify(repository).delete(existing);
    }
    private InterviewRound round(String name) { InterviewRound r=new InterviewRound(); r.setRoundName(name); r.setDescription("Description"); return r; }
}
