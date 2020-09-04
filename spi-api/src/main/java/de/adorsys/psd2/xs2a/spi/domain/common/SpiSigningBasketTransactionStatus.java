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

package de.adorsys.psd2.xs2a.spi.domain.common;

public enum SpiSigningBasketTransactionStatus {
    ACTC,  //AuthenticationObject and syntactical and semantical validation are successful"),
    RCVD,  //Payment initiation has been received by the receiving agent
    RJCT,  //Payment initiation or individual transaction included in the payment initiation has been rejected
    CANC, //Canceled
    PATC; // The payment initiation needs multiple authentications, where some but not yet all have been performed. Syntactical and semantical validations are successful.
}
