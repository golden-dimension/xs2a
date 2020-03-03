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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Standardised definition of reporting error information according to [RFC7807] in case of a HTTP error code 409 for signing baskets.
 */
@ApiModel(description = "Standardised definition of reporting error information according to [RFC7807] in case of a HTTP error code 409 for signing baskets. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-02-28T17:40:20.531650+02:00[Europe/Kiev]")

public class Error409SBS   {
  @JsonProperty("type")
  private String type = null;

  @JsonProperty("title")
  private String title = null;

  @JsonProperty("detail")
  private String detail = null;

  @JsonProperty("code")
  private String code = null;

  @JsonProperty("additionalErrors")
  @Valid
  private List<Error409SBSAdditionalErrors> additionalErrors = null;

  @JsonProperty("_links")
  private Map _links = null;

  public Error409SBS type(String type) {
    this.type = type;
    return this;
  }

    /**
     * A URI reference [RFC3986] that identifies the problem type. Remark For Future: These URI will be provided by NextGenPSD2 in future.
     *
     * @return type
     **/
    @ApiModelProperty(required = true, value = "A URI reference [RFC3986] that identifies the problem type. Remark For Future: These URI will be provided by NextGenPSD2 in future. ")
  @NotNull

@Size(max=70)

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Error409SBS title(String title) {
    this.title = title;
    return this;
  }

    /**
     * Short human readable description of error type. Could be in local language. To be provided by ASPSPs.
     * @return title
     **/
    @ApiModelProperty(value = "Short human readable description of error type. Could be in local language. To be provided by ASPSPs. ")

@Size(max=70)

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Error409SBS detail(String detail) {
    this.detail = detail;
    return this;
  }

    /**
     * Detailed human readable text specific to this instance of the error. XPath might be used to point to the issue generating the error in addition. Remark for Future: In future, a dedicated field might be introduced for the XPath.
     * @return detail
     **/
    @ApiModelProperty(value = "Detailed human readable text specific to this instance of the error. XPath might be used to point to the issue generating the error in addition. Remark for Future: In future, a dedicated field might be introduced for the XPath. ")

@Size(max=500)

  @JsonProperty("detail")
  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public Error409SBS code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull



  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Error409SBS additionalErrors(List<Error409SBSAdditionalErrors> additionalErrors) {
    this.additionalErrors = additionalErrors;
    return this;
  }

  public Error409SBS addAdditionalErrorsItem(Error409SBSAdditionalErrors additionalErrorsItem) {
    if (this.additionalErrors == null) {
      this.additionalErrors = new ArrayList<>();
    }
    this.additionalErrors.add(additionalErrorsItem);
    return this;
  }

  /**
   * Array of Error Information Blocks.  Might be used if more than one error is to be communicated
   * @return additionalErrors
  **/
  @ApiModelProperty(value = "Array of Error Information Blocks.  Might be used if more than one error is to be communicated ")

  @Valid


  @JsonProperty("additionalErrors")
  public List<Error409SBSAdditionalErrors> getAdditionalErrors() {
    return additionalErrors;
  }

  public void setAdditionalErrors(List<Error409SBSAdditionalErrors> additionalErrors) {
    this.additionalErrors = additionalErrors;
  }

  public Error409SBS _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    Error409SBS error409SBS = (Error409SBS) o;
    return Objects.equals(this.type, error409SBS.type) &&
    Objects.equals(this.title, error409SBS.title) &&
    Objects.equals(this.detail, error409SBS.detail) &&
    Objects.equals(this.code, error409SBS.code) &&
    Objects.equals(this.additionalErrors, error409SBS.additionalErrors) &&
    Objects.equals(this._links, error409SBS._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, detail, code, additionalErrors, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error409SBS {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    additionalErrors: ").append(toIndentedString(additionalErrors)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

