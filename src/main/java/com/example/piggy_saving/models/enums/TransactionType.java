package com.example.piggy_saving.models.enums;

public enum TransactionType {
    // --- Transfers between users ---
    P2P_TRANSFER,           // User → User (like Zelle, Venmo, peer-to-peer)
    EXTERNAL_TRANSFER,      // To other bank accounts (ACH / wire)

    // --- Sub-account / savings goal transactions ---
    GOAL_CONTRIBUTION,      // Main → Piggy Goal / Savings sub-account
    GOAL_WITHDRAWAL,        // Piggy Goal → Main (includes early break / partial withdrawal)
    INTEREST_CREDIT,        // Interest credited to goal or main account
    PENALTY_FEE,            // Fee for early withdrawal or breaking goal

    // --- Card / payment transactions ---
    CARD_PAYMENT,           // Debit / credit card purchase
    CARD_REFUND,            // Refund to card
    BILL_PAYMENT,           // Utility, loan, or other bill payment
    DIRECT_DEBIT,           // Recurring debit (subscription, loan)

    // --- Bank internal reversals / corrections ---
    REVERSAL,               // Transaction reversed / canceled
    FEE_CHARGE,             // Bank service fee (monthly, overdraft)
    CASH_DEPOSIT,           // Cash deposited to account
    CASH_WITHDRAWAL,        // Cash withdrawal from ATM / branch

    // --- Optional / internal bank bookkeeping ---
    LOAN_DISBURSEMENT,      // Bank loans credited to account
    LOAN_PAYMENT            // Payment towards loan

}
