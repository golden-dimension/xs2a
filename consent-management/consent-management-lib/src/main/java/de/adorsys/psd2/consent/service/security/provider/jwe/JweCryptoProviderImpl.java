/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.security.provider.jwe;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.provider.AbstractCryptoProvider;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import lombok.extern.slf4j.Slf4j;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Optional;

@Slf4j
public class JweCryptoProviderImpl extends AbstractCryptoProvider implements CryptoProvider {
    private static final EncryptionMethod METHOD = EncryptionMethod.A256GCM;
    private static final JWEAlgorithm ALGORITHM = JWEAlgorithm.A256GCMKW;


    public JweCryptoProviderImpl(String cryptoProviderId, int keyLength, int hashIterations, String skfAlgorithm) {
        super(keyLength, hashIterations, skfAlgorithm, cryptoProviderId);
    }

    @Override
    public Optional<EncryptedData> encryptData(byte[] data, String password) {
        try {
            Payload payload = new Payload(data);
            Key secretKey = getSecretKey(password);

            JWEHeader header = new JWEHeader(ALGORITHM, METHOD);
            JWEObject jweObject = new JWEObject(header, payload);
            JWEEncrypter encrypter = new AESEncrypter(secretKey.getEncoded());

            jweObject.encrypt(encrypter);
            String encryptedData = jweObject.serialize();

            return Optional.of(new EncryptedData(encryptedData.getBytes()));

        } catch (GeneralSecurityException | JOSEException e) {
            log.info("Error encryption data: ", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<DecryptedData> decryptData(byte[] data, String password) {
        try {
            Key secretKey = getSecretKey(password);

            JWEObject jweObject = JWEObject.parse(new String(data));
            JWEDecrypter decrypter = new AESDecrypter(secretKey.getEncoded());
            jweObject.decrypt(decrypter);

            return Optional.of(new DecryptedData(jweObject.getPayload().toBytes()));
        } catch (Exception e) {
            log.info("Error encryption data. Data can't be parsed : ", e);
        }

        return Optional.empty();
    }
}
