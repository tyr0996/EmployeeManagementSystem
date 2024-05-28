package hu.martin.ems.core.apiresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CurrencyAPI {
    private String provider;
    @JsonProperty("WARNING_UPGRADE_TO_V6")
    private String warningUpgradeToV6;
    private String terms;
    private String base;
    private String date;
    @JsonProperty("time_last_updated")
    private Long timeLastUpdated;
    private CurrencyResponse rates;

    @Transient
    private LocalDate validDate;

    public LocalDate getValidDate() {
        String[] date = this.date.split("-");
        LocalDate ld = LocalDate.of(Integer.parseInt(date[0]),
                Integer.parseInt(date[1]),
                Integer.parseInt(date[2]));
        return ld;
    }
}
