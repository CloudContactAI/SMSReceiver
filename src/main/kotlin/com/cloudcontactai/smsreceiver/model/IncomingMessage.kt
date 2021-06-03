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

package com.cloudcontactai.smsreceiver.model

import com.cloudcontactai.smsreceiver.model.type.PhoneActionPayload
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import javax.persistence.Column

@Document(collection = "incoming_message")
class IncomingMessage {
    @Id
    var id: String? = null

    @Field("body_request")
    var bodyRequest: Map<String,String>? = null

    @CreatedDate
    @Field("created_date")
    var createdDate = Instant.now()

    @Column(name = "auto_response")
    var autoResponse = false

    @Column(name = "auto_archive")
    var autoArchive = false

    @Column(name = "opt_out")
    var optOut: Boolean = false

    @Column(name = "opt_in")
    var optIn: Boolean = false

    @Column(name = "actions")
    var actions: List<PhoneActionPayload>? = listOf()
}
