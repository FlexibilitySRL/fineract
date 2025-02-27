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
package org.apache.fineract.infrastructure.codes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.CodeConstants.CodevalueJSONinputParams;
import org.apache.fineract.infrastructure.codes.data.CodeData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/codes/{codeId}/codevalues")
@Component
@Scope("singleton")
@Tag(name = "Code Values", description = "Code and code values: Codes represent a specific category of data, their code values are a specific instance of that category.\n"
        + "\n"
        + "Codes are mostly system defined which means the code itself comes out of the box and cannot be modified however its code values can be. e.g. 'Customer Identifier', it defaults to a code value of 'Passport' but could be 'Drivers License, National Id' etc")
public class CodeValuesApiResource {

    /**
     * The set of parameters that are supported in response for {@link CodeData}
     */
    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(CodevalueJSONinputParams.CODEVALUE_ID.getValue(), CodevalueJSONinputParams.NAME.getValue(),
                    CodevalueJSONinputParams.POSITION.getValue(), CodevalueJSONinputParams.IS_MANDATORY.getValue(),
                    CodevalueJSONinputParams.DESCRIPTION.getValue()));
    private final String resourceNameForPermissions = "CODEVALUE";

    private final PlatformSecurityContext context;
    private final CodeValueReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<CodeValueData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public CodeValuesApiResource(final PlatformSecurityContext context, final CodeValueReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<CodeValueData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Code Values", description = "Returns the list of Code Values for a given Code\n" + "\n"
            + "Example Requests:\n" + "\n" + "codes/1/codevalues", parameters = @Parameter(name = "codeId", description = "co"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A List of code values for a given code", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CodeValuesApiResourceSwagger.GetCodeValuesDataResponse.class)))) })
    public String retrieveAllCodeValues(@Context final UriInfo uriInfo,
            @QueryParam("byName") @Parameter(description = "type") final Boolean byName,
            @PathParam("codeId") @Parameter(description = "codeId") final Long codeId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        Collection<CodeValueData> codeValues = this.readPlatformService.retrieveAllCodeValues(codeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, codeValues, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{codeValueId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Code description", description = "Returns the details of a Code Value\n" + "\n" + "Example Requests:\n"
            + "\n" + "codes/1/codevalues/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CodeValuesApiResourceSwagger.GetCodeValuesDataResponse.class))) })
    public String retrieveCodeValue(@Context final UriInfo uriInfo,
            @PathParam("codeValueId") @Parameter(description = "codeValueId") final String codeValueId,
            @PathParam("codeId") @Parameter(description = "codeId") final Long codeId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        CodeValueData codeValue;
        if (NumberUtils.isParsable(codeValueId)) {
            codeValue = this.readPlatformService.retrieveCodeValue(Long.parseLong(codeValueId));
        } else {
            codeValue = this.readPlatformService.retrieveCodeValueByName(codeValueId);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, codeValue, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Code description", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CodeValuesApiResourceSwagger.PostCodeValuesDataRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CodeValuesApiResourceSwagger.PostCodeValueDataResponse.class))) })
    public String createCodeValue(@PathParam("codeId") @Parameter(description = "codeId") final Long codeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCodeValue(codeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{codeValueId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Code description", description = "Updates the details of a code description.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CodeValuesApiResourceSwagger.PutCodeValuesDataRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CodeValuesApiResourceSwagger.PutCodeValueDataResponse.class))) })
    public String updateCodeValue(@PathParam("codeId") @Parameter(description = "codeId") final Long codeId,
            @PathParam("codeValueId") @Parameter(description = "codeValueId") final Long codeValueId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCodeValue(codeId, codeValueId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{codeValueId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Code description", description = "Deletes a code description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CodeValuesApiResourceSwagger.DeleteCodeValueDataResponse.class))) })
    public String deleteCodeValue(@PathParam("codeId") @Parameter(description = "codeId") final Long codeId,
            @PathParam("codeValueId") @Parameter(description = "codeValueId") final Long codeValueId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCodeValue(codeId, codeValueId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
