package com.validatorapi.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.validatorapi.dto.PhoneValidationResponse;
import com.validatorapi.model.ValidationHistory;
import com.validatorapi.repository.ValidationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneValidatorService {

    private final ValidationHistoryRepository validationHistoryRepository;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private final PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();

    @Cacheable(value = "phoneValidation", key = "#phoneInput + '_' + #defaultCountry")
    public PhoneValidationResponse validate(String phoneInput, String defaultCountry) {
        long startTime = System.currentTimeMillis();

        phoneInput = phoneInput.trim();
        if (defaultCountry == null || defaultCountry.isEmpty()) {
            defaultCountry = "US"; // Défaut si non spécifié
        }

        PhoneValidationResponse.PhoneValidationResponseBuilder builder =
                PhoneValidationResponse.builder()
                        .phone(phoneInput)
                        .checkedAt(LocalDateTime.now());

        try {
            // Parse le numéro
            PhoneNumber number = phoneUtil.parse(phoneInput, defaultCountry.toUpperCase());

            // Validation rapide
            boolean isPossible = phoneUtil.isPossibleNumber(number);
            boolean isValid = phoneUtil.isValidNumber(number);

            builder.valid(isValid);

            if (!isValid) {
                builder.riskScore(0)
                        .validationTimeMs(System.currentTimeMillis() - startTime);
                saveHistory(phoneInput, false, 0, System.currentTimeMillis() - startTime);
                return builder.build();
            }

            // Extraction informations
            int countryCode = number.getCountryCode();
            String regionCode = phoneUtil.getRegionCodeForNumber(number);

            builder.countryCode(regionCode)
                    .countryPrefix(countryCode);

            // Formatage
            String nationalFormat = phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            String internationalFormat = phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            String e164Format = phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);

            builder.nationalFormat(nationalFormat)
                    .internationalFormat(internationalFormat)
                    .e164Format(e164Format);

            // Détection type de ligne
            PhoneNumberUtil.PhoneNumberType numberType = phoneUtil.getNumberType(number);
            String type = mapPhoneNumberType(numberType);
            builder.type(type);

            // Nom du pays
            String countryName = geocoder.getDescriptionForNumber(number, Locale.ENGLISH);
            builder.country(countryName);

            // Timezone (approximatif basé sur pays)
            String timezone = getTimezoneForCountry(regionCode);
            builder.timezone(timezone);

            // Risk score basé sur le type
            int riskScore = calculateRiskScore(numberType, isPossible);
            builder.riskScore(riskScore);

            long validationTime = System.currentTimeMillis() - startTime;
            builder.validationTimeMs(validationTime);

            saveHistory(phoneInput, isValid, riskScore, validationTime);

            log.info("Phone validated: {} - valid: {} - type: {} - time: {}ms",
                    e164Format, isValid, type, validationTime);

            return builder.build();

        } catch (NumberParseException e) {
            log.debug("Phone parsing failed for: {} - {}", phoneInput, e.getMessage());

            builder.valid(false)
                    .riskScore(0)
                    .validationTimeMs(System.currentTimeMillis() - startTime);

            saveHistory(phoneInput, false, 0, System.currentTimeMillis() - startTime);

            return builder.build();
        }
    }

    private String mapPhoneNumberType(PhoneNumberUtil.PhoneNumberType type) {
        return switch (type) {
            case MOBILE -> "MOBILE";
            case FIXED_LINE -> "FIXED_LINE";
            case FIXED_LINE_OR_MOBILE -> "FIXED_OR_MOBILE";
            case TOLL_FREE -> "TOLL_FREE";
            case PREMIUM_RATE -> "PREMIUM_RATE";
            case SHARED_COST -> "SHARED_COST";
            case VOIP -> "VOIP";
            case PERSONAL_NUMBER -> "PERSONAL";
            case PAGER -> "PAGER";
            case UAN -> "UAN";
            case VOICEMAIL -> "VOICEMAIL";
            default -> "UNKNOWN";
        };
    }

    private int calculateRiskScore(PhoneNumberUtil.PhoneNumberType type, boolean isPossible) {
        int score = 100;

        if (!isPossible) score -= 50;

        return switch (type) {
            case MOBILE, FIXED_LINE -> score;
            case VOIP -> score - 10;
            case PREMIUM_RATE -> score - 40;
            case TOLL_FREE -> score - 5;
            default -> score - 20;
        };
    }

    private String getTimezoneForCountry(String countryCode) {
        // Mapping simplifié des timezones principales
        return switch (countryCode) {
            case "FR" -> "Europe/Paris";
            case "GB" -> "Europe/London";
            case "US" -> "America/New_York";
            case "DE" -> "Europe/Berlin";
            case "ES" -> "Europe/Madrid";
            case "IT" -> "Europe/Rome";
            case "CA" -> "America/Toronto";
            case "AU" -> "Australia/Sydney";
            case "JP" -> "Asia/Tokyo";
            case "CN" -> "Asia/Shanghai";
            default -> "UTC";
        };
    }

    @Async
    protected void saveHistory(String phone, boolean isValid, int riskScore, long validationTime) {
        ValidationHistory history = new ValidationHistory();
        history.setValidationType("PHONE");
        history.setInputValue(phone);
        history.setIsValid(isValid);
        history.setRiskScore(riskScore);
        history.setValidationTimeMs(validationTime);
        validationHistoryRepository.save(history);
    }
}