package com.jacknie.doongji.banksalad.config

import com.jacknie.fd.*

class FilePolicyImpl: FilePolicy {

    private val userHome = System.getProperty("user.home")

    override fun generateFilename(source: FileSource): String = getUUIDFilename(source)

    override fun getSystemPath(source: FileSource): String = getTodayIndexPath("$userHome/file-delivery", source)

    override fun verifyFileSource(source: FileSource): FileVerification = FileVerification(source,  emptyList())
}