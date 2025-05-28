package hu.martin.ems.pages.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PerformRestoreFailedResult {
    String[] deletedData;
}
