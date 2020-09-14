package de.adorsys.psd2.consent.domain.sb;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity(name = "signing_basket_tpp_information")
@ApiModel(description = "Signing basket tpp information", value = "SigningBasketTppInformationEntity")
public class SigningBasketTppInformationEntity {
    @Id
    @Column(name="signing_basket_tpp_information_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "signing_basket_tpp_information_generator")
    @SequenceGenerator(name = "signing_basket_tpp_information_generator", sequenceName = "signing_basket_tpp_information_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tpp_info_id", nullable = false)
    @ApiModelProperty(value = "Information about TPP", required = true)
    private TppInfoEntity tppInfo;

    @Column(name = "tpp_ntfc_uri")
    private String tppNotificationUri;

    @Column(name = "tpp_redirect_preferred", nullable = false)
    @ApiModelProperty(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.", required = true, example = "false")
    private boolean tppRedirectPreferred;

    @ElementCollection
    @CollectionTable(name = "signing_basket_tpp_ntfc", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "notification_mode", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private List<NotificationSupportedMode> tppNotificationContentPreferred;
}
