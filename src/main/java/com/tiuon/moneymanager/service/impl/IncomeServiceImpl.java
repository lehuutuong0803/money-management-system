package com.tiuon.moneymanager.service.impl;

import com.tiuon.moneymanager.dto.IncomeDto;
import com.tiuon.moneymanager.entity.CategoryEntity;
import com.tiuon.moneymanager.entity.IncomeEntity;
import com.tiuon.moneymanager.entity.ProfileEntity;
import com.tiuon.moneymanager.mapper.IncomeMapper;
import com.tiuon.moneymanager.repository.CategoryRepository;
import com.tiuon.moneymanager.repository.IncomeRepository;
import com.tiuon.moneymanager.service.IIcomeService;
import com.tiuon.moneymanager.service.IProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IIcomeService {
    private final IProfileService iProfileService;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    
    // save Income instance
    public IncomeDto addIncome(IncomeDto incomeDto) {
        ProfileEntity profile = iProfileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(incomeDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category doesn't exist"));
        IncomeEntity newIncomeEntity = IncomeMapper.toEntity(incomeDto, profile, category);
        newIncomeEntity = incomeRepository.save(newIncomeEntity);
        return IncomeMapper.toDto(newIncomeEntity);
    }

    // retrieve all incomes for the current month/based on the start date and end date
    public List<IncomeDto> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity profile = iProfileService.getCurrentProfile();
        LocalDate now  = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> incomeEntityList = incomeRepository.findByProfileIdAndDateBetween(profile.getId(),
                startDate, endDate);
        return incomeEntityList.stream().map(IncomeMapper::toDto).toList();
    }

    // delete income by id for current user
    public void deleteIncome(Long incomeId) {
        ProfileEntity profileEntity = iProfileService.getCurrentProfile();
        IncomeEntity incomeEntity = incomeRepository.findById(incomeId).orElseThrow(
                () -> new RuntimeException("Income doesn't exist")
        );
        if (!incomeEntity.getProfile().getId().equals(profileEntity.getId())) {
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(incomeEntity);
    }

    // Get latest 5 incomes for current user
    public List<IncomeDto> getLatest5IncomeForCurrentUser() {
        ProfileEntity profile = iProfileService.getCurrentProfile();
        List<IncomeEntity> incomeEntityList = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return incomeEntityList.stream().map(IncomeMapper::toDto).toList();
    }

    // Get total expenses for current user
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = iProfileService.getCurrentProfile();
        BigDecimal totalIncome = incomeRepository.findTotalExpenseByProfileId(profile.getId());
        return  totalIncome != null ? totalIncome : BigDecimal.ZERO;
    }

    // Filter incomes
    public List<IncomeDto> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = iProfileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                profile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(IncomeMapper::toDto).toList();
    }
}
