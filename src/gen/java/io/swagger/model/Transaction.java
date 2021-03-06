/*
 * Manage Bitcoins for users.
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package io.swagger.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * incoming or outgoing tx fullfilling an invoice
 */
@ApiModel(description = "incoming or outgoing tx fullfilling an invoice")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-25T09:55:46.951Z")
public class Transaction   {
  @JsonProperty("txid")
  private String txid = null;

  /**
   * confidence state
   */
  public enum StateEnum {
    BUILDING("building"),
    
    PENDING("pending"),
    
    DEAD("dead"),
    
    UNKNOWN("unknown"),
    
    CONFLICT("conflict");

    private String value;

    StateEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StateEnum fromValue(String text) {
      for (StateEnum b : StateEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("state")
  private StateEnum state = StateEnum.UNKNOWN;

  @JsonProperty("depthInBlocks")
  private Integer depthInBlocks = null;

  @JsonProperty("amount")
  private Integer amount = null;

  public Transaction txid(String txid) {
    this.txid = txid;
    return this;
  }

  /**
   * the txid
   * @return txid
   **/
  @JsonProperty("txid")
  @ApiModelProperty(value = "the txid")
  public String getTxid() {
    return txid;
  }

  public void setTxid(String txid) {
    this.txid = txid;
  }

  public Transaction state(StateEnum state) {
    this.state = state;
    return this;
  }

  /**
   * confidence state
   * @return state
   **/
  @JsonProperty("state")
  @ApiModelProperty(value = "confidence state")
  public StateEnum getState() {
    return state;
  }

  public void setState(StateEnum state) {
    this.state = state;
  }

  public Transaction depthInBlocks(Integer depthInBlocks) {
    this.depthInBlocks = depthInBlocks;
    return this;
  }

  /**
   * depth of a building transaction in blocks
   * @return depthInBlocks
   **/
  @JsonProperty("depthInBlocks")
  @ApiModelProperty(value = "depth of a building transaction in blocks")
  public Integer getDepthInBlocks() {
    return depthInBlocks;
  }

  public void setDepthInBlocks(Integer depthInBlocks) {
    this.depthInBlocks = depthInBlocks;
  }

  public Transaction amount(Integer amount) {
    this.amount = amount;
    return this;
  }

  /**
   * The amount of satoshies received by this tx
   * @return amount
   **/
  @JsonProperty("amount")
  @ApiModelProperty(value = "The amount of satoshies received by this tx")
  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Transaction transaction = (Transaction) o;
    return Objects.equals(this.txid, transaction.txid) &&
        Objects.equals(this.state, transaction.state) &&
        Objects.equals(this.depthInBlocks, transaction.depthInBlocks) &&
        Objects.equals(this.amount, transaction.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(txid, state, depthInBlocks, amount);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Transaction {\n");
    
    sb.append("    txid: ").append(toIndentedString(txid)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    depthInBlocks: ").append(toIndentedString(depthInBlocks)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

