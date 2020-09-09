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

package de.adorsys.psd2.consent.domain.sb;

import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "signing_basket")
@EqualsAndHashCode(callSuper = true)
public class SigningBasket extends InstanceDependableEntity implements Authorisable {
    @Id
    @Column(name = "signing_basket_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "signing_basket_generator")
    @SequenceGenerator(name = "signing_basket_generator", sequenceName = "signing_basket_id_seq",
        allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "signing_basket_consents",
        joinColumns = @JoinColumn(name = "signing_basket_id"),
        inverseJoinColumns = @JoinColumn(name = "consent_id"))
    private List<ConsentEntity> consents = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "signing_basket_payments",
        joinColumns = @JoinColumn(name = "signing_basket_id"),
        inverseJoinColumns = @JoinColumn(name = "payment_id"))
    private List<PisCommonPaymentData> payments = new ArrayList<>();

    @Column(name = "transaction_status", nullable = false)
    private String transactionStatus;

    @Column(name = "internal_request_id")
    private String internalRequestId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "authorisation_template_id", nullable = false)
    private AuthorisationTemplateEntity authorisationTemplate = new AuthorisationTemplateEntity();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "signing_basket_psu_data",
        joinColumns = @JoinColumn(name = "signing_basket_id"),
        inverseJoinColumns = @JoinColumn(name = "psu_data_id"))
    private List<PsuData> psuDataList = new ArrayList<>();

    @Column(name = "multilevel_sca_required")
    private boolean multilevelScaRequired;

    @Column(name = "instance_id")
    private String instanceId;

    @Override
    public String getInternalRequestId(AuthorisationType authorisationType) {
        if (authorisationType == AuthorisationType.SIGNING_BASKET) {
            return internalRequestId;
        }

        throw new IllegalArgumentException("Invalid authorisation type: " + authorisationType);
    }
}
