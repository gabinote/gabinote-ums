package com.gabinote.ums.common.util.exception.service

class ResourceDuplicate(
    name: String,
    identifier: String,
    identifierType: String? = null,
) : ServiceException() {

    override val errorMessage: String="$name already exists with identifier($identifierType): $identifier"


    override val logMessage: String = errorMessage

}