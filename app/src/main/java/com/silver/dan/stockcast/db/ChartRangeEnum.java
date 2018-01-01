package com.silver.dan.stockcast.db;

import com.silver.dan.stockcast.R;

/**
 * Created by dan on 12/29/17.
 */

public enum ChartRangeEnum {
    ONE_WEEK(0, R.string.s1_week),
    TWO_WEEKS(1, R.string.s2_weeks),
    ONE_MONTH(2, R.string.s1_month),
    TWO_MONTHS(3, R.string.s2_months);

    public int value;
    public int stringRes;

    ChartRangeEnum(int i, int stringRes) {
        this.value = i;
        this.stringRes = stringRes;
    }

    public static ChartRangeEnum fromInt(int displayMode) {
        switch (displayMode) {
            case 0:
                return ChartRangeEnum.ONE_WEEK;
            case 1:
                return ChartRangeEnum.TWO_WEEKS;
            case 2:
                return ChartRangeEnum.ONE_MONTH;
            case 3:
                return ChartRangeEnum.TWO_MONTHS;
        }

        return null;
    }
}