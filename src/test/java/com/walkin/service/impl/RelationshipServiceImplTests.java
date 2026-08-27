package com.walkin.service.impl;

import com.walkin.entity.*;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelationshipServiceImplTests {
    @Test void companyRoundCrudPreservesRelationships() {
        CompanyCustomRoundRepository repository=mock(CompanyCustomRoundRepository.class);
        CompanyCustomRoundServiceImpl service=new CompanyCustomRoundServiceImpl(repository);
        CompanyCustomRound existing=new CompanyCustomRound(), update=new CompanyCustomRound();
        Company company=new Company(); InterviewRound round=new InterviewRound(); update.setCompany(company); update.setInterviewRound(round);
        when(repository.findById(1)).thenReturn(Optional.of(existing)); when(repository.save(existing)).thenReturn(existing);
        assertSame(existing, service.updateCompanyCustomRound(1, update)); assertSame(company, existing.getCompany()); assertSame(round, existing.getInterviewRound());
        service.deleteCompanyCustomRound(1); verify(repository).delete(existing);
    }
    @Test void applicationUpdateKeepsDateWhenOmitted() {
        StudentApplicationRepository repository=mock(StudentApplicationRepository.class);
        StudentApplicationServiceImpl service=new StudentApplicationServiceImpl(repository);
        StudentApplication existing=new StudentApplication(), update=new StudentApplication(); LocalDateTime original=LocalDateTime.of(2026,1,1,10,0);
        existing.setApplicationDate(original); update.setStudent(new Student()); update.setCompany(new Company());
        when(repository.findById(2)).thenReturn(Optional.of(existing)); when(repository.save(existing)).thenReturn(existing);
        service.updateStudentApplication(2, update); assertEquals(original, existing.getApplicationDate()); assertSame(update.getStudent(), existing.getStudent());
    }
    @Test void selectionUpdateCopiesStatusAndRelations() {
        StudentRoundSelectionRepository repository=mock(StudentRoundSelectionRepository.class);
        StudentRoundSelectionServiceImpl service=new StudentRoundSelectionServiceImpl(repository);
        StudentRoundSelection existing=new StudentRoundSelection(), update=new StudentRoundSelection();
        update.setStudent(new Student()); update.setCompanyCustomRound(new CompanyCustomRound()); update.setStatus(StudentRoundSelection.SelectionStatus.SELECTED);
        when(repository.findById(3)).thenReturn(Optional.of(existing)); when(repository.save(existing)).thenReturn(existing);
        assertSame(existing, service.updateStudentRoundSelection(3, update)); assertEquals(StudentRoundSelection.SelectionStatus.SELECTED, existing.getStatus());
    }
    @Test void relationshipServicesRejectUnknownIdsWithoutDeleting() {
        StudentApplicationRepository applications=mock(StudentApplicationRepository.class); when(applications.findById(9)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> new StudentApplicationServiceImpl(applications).deleteStudentApplication(9));
        verify(applications, never()).delete(any());
        StudentRoundSelectionRepository selections=mock(StudentRoundSelectionRepository.class); when(selections.findById(9)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> new StudentRoundSelectionServiceImpl(selections).getStudentRoundSelectionById(9));
    }
}
