/* Copyright 2021 Cloud Contact AI, Inc. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.cloudcontactai.smsreceiver.converter

import com.cloudcontactai.smsreceiver.model.type.PhoneActionPayload
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class PhoneActionPayloadConverter : AttributeConverter<PhoneActionPayload?, String> {
    companion object {
        private val logger = LoggerFactory.getLogger(PhoneActionPayloadConverter::class.java)
        private val objectMapper = ObjectMapper()
    }

    override fun convertToDatabaseColumn(objectValue: PhoneActionPayload?): String {
        var dbValue: String? = null

        objectValue?.let {
            try {
                dbValue = objectMapper.writeValueAsString(it)
            } catch (e: JsonProcessingException) {
                logger.error("JSON writing error", e)
            }
        }

        return dbValue ?: ""
    }

    override fun convertToEntityAttribute(dbValue: String?): PhoneActionPayload {
        var objectValue: PhoneActionPayload? = null

        dbValue?.let {
            try {
                objectValue = objectMapper.readValue(dbValue, PhoneActionPayload::class.java)
            } catch (e: IOException) {
                logger.error("JSON reading error", e)
            }
        }

        return objectValue ?: (PhoneActionPayload())
    }

}