package com.validatorapi.service;

import com.validatorapi.dto.IbanValidationResponse;
import com.validatorapi.model.ValidationHistory;
import com.validatorapi.repository.ValidationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IbanValidatorService {

    private final ValidationHistoryRepository validationHistoryRepository;

    @Cacheable(value = "ibanValidation", key = "#ibanInput")
    public IbanValidationResponse validate(String ibanInput) {
        long startTime = System.currentTimeMillis();

        // Nettoyage
        String iban = ibanInput.replaceAll("\\s+", "").toUpperCase();

        IbanValidationResponse.IbanValidationResponseBuilder builder =
                IbanValidationResponse.builder()
                        .iban(iban)
                        .checkedAt(LocalDateTime.now());

        try {
            // Validation avec iban4j
            IbanUtil.validate(iban);

            builder.valid(true);

            // Extraction informations
            String countryCode = iban.substring(0, 2);
            String checkDigits = iban.substring(2, 4);

            builder.countryCode(countryCode)
                    .checkDigits(checkDigits);

            // Extraction codes bancaires (dépend du pays)
            extractBankInfo(iban, countryCode, builder);

            // Formatage avec espaces
            String formatted = formatIban(iban);
            builder.ibanFormatted(formatted);

            // Nom du pays
            String countryName = getCountryName(countryCode);
            builder.country(countryName);

            long validationTime = System.currentTimeMillis() - startTime;
            builder.validationTimeMs(validationTime);

            saveHistory(iban, true, validationTime);

            log.info("IBAN validated: {} - valid: true - time: {}ms",
                    countryCode + "**" + iban.substring(iban.length() - 4), validationTime);

            return builder.build();

        } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
            log.debug("IBAN validation failed for: {} - {}", iban, e.getMessage());

            builder.valid(false)
                    .validationTimeMs(System.currentTimeMillis() - startTime);

            saveHistory(iban, false, System.currentTimeMillis() - startTime);

            return builder.build();
        }
    }

    private void extractBankInfo(String iban, String countryCode,
                                 IbanValidationResponse.IbanValidationResponseBuilder builder) {

        // Format spécifique par pays (exemples)
        switch (countryCode) {
            case "FR": // France: FR76 3000 6000 0112 3456 7890 189
                if (iban.length() >= 14) {
                    builder.bankCode(iban.substring(4, 9))
                            .branchCode(iban.substring(9, 14))
                            .accountNumber(iban.substring(14));
                }
                break;

            case "DE": // Allemagne: DE89 3704 0044 0532 0130 00
                if (iban.length() >= 18) {
                    builder.bankCode(iban.substring(4, 12))
                            .accountNumber(iban.substring(12));
                }
                break;

            case "GB": // UK: GB82 WEST 1234 5698 7654 32
                if (iban.length() >= 18) {
                    builder.bankCode(iban.substring(4, 8))
                            .branchCode(iban.substring(8, 14))
                            .accountNumber(iban.substring(14));
                }
                break;

            case "ES": // Espagne: ES91 2100 0418 4502 0005 1332
                if (iban.length() >= 14) {
                    builder.bankCode(iban.substring(4, 8))
                            .branchCode(iban.substring(8, 12))
                            .accountNumber(iban.substring(12));
                }
                break;

            default:
                // Format générique
                if (iban.length() > 10) {
                    builder.accountNumber(iban.substring(4));
                }
        }
    }

    private String formatIban(String iban) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < iban.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(iban.charAt(i));
        }
        return formatted.toString();
    }

    private String getCountryName(String code) {
        return switch (code) {
            case "FR" -> "France";
            case "DE" -> "Germany";
            case "GB" -> "United Kingdom";
            case "ES" -> "Spain";
            case "IT" -> "Italy";
            case "NL" -> "Netherlands";
            case "BE" -> "Belgium";
            case "CH" -> "Switzerland";
            case "AT" -> "Austria";
            case "PT" -> "Portugal";
            case "IE" -> "Ireland";
            case "PL" -> "Poland";
            case "SE" -> "Sweden";
            case "NO" -> "Norway";
            case "DK" -> "Denmark";
            case "FI" -> "Finland";
            default -> code;
        };
    }

    @Async
    protected void saveHistory(String iban, boolean isValid, long validationTime) {
        ValidationHistory history = new ValidationHistory();
        history.setValidationType("IBAN");
        history.setInputValue(iban.substring(0, 4) + "****" + iban.substring(iban.length() - 4));
        history.setIsValid(isValid);
        history.setValidationTimeMs(validationTime);
        validationHistoryRepository.save(history);
    }
}
