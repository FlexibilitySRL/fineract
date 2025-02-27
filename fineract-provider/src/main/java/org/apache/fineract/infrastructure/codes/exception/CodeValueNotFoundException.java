/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.codes.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class CodeValueNotFoundException extends AbstractPlatformResourceNotFoundException {

    public CodeValueNotFoundException(final Long id) {
        super("error.msg.codevalue.id.invalid", "Code value with identifier " + id + " does not exist", id);
    }

    public CodeValueNotFoundException(final String code) {
        super("error.msg.codevalue.name.invalid", "Code value with name " + code + " does not exist", code);
    }

    public CodeValueNotFoundException(final String codeName, final Long id) {
        super("error.msg.codevalue.codename.id.combination.invalid",
                "Code value with identifier " + id + " does not exist for a code with name " + codeName, id, codeName);
    }

    public CodeValueNotFoundException(final String codeName, final String label) {
        super("error.msg.codevalue.codename.id.combination.invalid",
                "Code value with label " + label + " does not exist for a code with name " + codeName, label, codeName);
    }

    public CodeValueNotFoundException(Long id, EmptyResultDataAccessException e) {
        super("error.msg.codevalue.id.invalid", "Code value with identifier " + id + " does not exist", id, e);
    }

    public CodeValueNotFoundException(String code, EmptyResultDataAccessException e) {
        super("error.msg.codevalue.id.invalid", "Code value with code " + code + " does not exist", code, e);
    }
}
