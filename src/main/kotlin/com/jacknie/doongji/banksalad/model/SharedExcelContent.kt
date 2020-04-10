package com.jacknie.doongji.banksalad.model

data class SharedExcelContent(

        var userId: String,

        var records: List<HouseholdAccounts>
)