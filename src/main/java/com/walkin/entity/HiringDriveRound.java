package com.walkin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(
        name = "hiring_drive_round",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hiring_drive_round_assignment",
                        columnNames = {"drive_id", "company_round_id"}),
                @UniqueConstraint(
                        name = "uk_hiring_drive_round_order",
                        columnNames = {"drive_id", "round_order"})
        })
public class HiringDriveRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drive_round_id", nullable = false)
    private Integer driveRoundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", nullable = false)
    @NotNull
    private HiringDrive hiringDrive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_round_id", nullable = false)
    @NotNull
    private CompanyCustomRound companyRound;

    @Column(name = "round_order", nullable = false)
    @Positive
    private Integer roundOrder;

    public Integer getDriveRoundId() {
        return driveRoundId;
    }

    public HiringDrive getHiringDrive() {
        return hiringDrive;
    }

    public void setHiringDrive(HiringDrive hiringDrive) {
        this.hiringDrive = hiringDrive;
    }

    public CompanyCustomRound getCompanyRound() {
        return companyRound;
    }

    public void setCompanyRound(CompanyCustomRound companyRound) {
        this.companyRound = companyRound;
    }

    public Integer getRoundOrder() {
        return roundOrder;
    }

    public void setRoundOrder(Integer roundOrder) {
        this.roundOrder = roundOrder;
    }
}
