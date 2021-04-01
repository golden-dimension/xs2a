/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import de.adorsys.psd2.event.service.model.AspspEvent;
import io.swagger.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/events")
@Api(value = "aspsp-api/v1/events", tags = CmsAspspApiTagName.ASPSP_EVENTS)
public interface CmsAspspEventApi {

    @GetMapping(path = "/")
    @ApiOperation(value = "Returns a list of Event objects between two dates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK")})
    ResponseEntity<List<AspspEvent>> getEventsForDates(
        @ApiParam(value = "Start date", example = "2010-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "start-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
        @ApiParam(value = "End date", example = "2030-01-01T00:00:00Z", required = true)
        @RequestHeader(value = "end-date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
        @ApiParam(value = "Bank instance ID")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @ApiParam(value = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @ApiParam(value = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);
}
