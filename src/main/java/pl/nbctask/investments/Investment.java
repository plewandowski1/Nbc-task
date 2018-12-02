package pl.nbctask.investments;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import pl.nbctask.enums.FundType;
import pl.nbctask.exceptions.InvestedAmountException;
import pl.nbctask.exceptions.MandatoryFundInvestmentException;
import pl.nbctask.model.InvestmentFund;
import pl.nbctask.model.Report;
import pl.nbctask.model.ReportRow;

/**
 *
 * @author plewandowski
 */
public abstract class Investment {

    public Report calculate(Integer amountForInvest, List<InvestmentFund> investmentFunds)
            throws MandatoryFundInvestmentException, 
            InvestedAmountException {
        if (!isMandatoryFundTypesPresent(investmentFunds)) {
            throw new MandatoryFundInvestmentException("Investment funds must contains equal amount of fund types to declared in investment style");
        }
        if(amountForInvest < 1){
            throw new InvestedAmountException("You cannot invest non positive amount of money");
        }

        Report report = new Report();
        int investedAmount = 0;

        for (FundType fundType : FundType.values()) {
            List<InvestmentFund> fundsForType = investmentFunds
                    .stream()
                    .filter(e -> e.getFundType() == fundType)
                    .collect(Collectors.toList());

            Integer percentageForFund = getPercentages().get(fundType);

            int amountForType = amountForInvest * percentageForFund / 100;
            int partForType = amountForType / fundsForType.size();

            int restForType = 0;

            if (partForType != 0) {
                restForType = amountForType % partForType;
            }

            for (InvestmentFund investmentFund : fundsForType) {             
                ReportRow reportRow = new ReportRow(investmentFund, partForType + restForType);
                report.addReportRow(reportRow);
                restForType = 0;
            }

            investedAmount += amountForType;
        }

        int uninvestedAmount = 0;
        if (investedAmount != 0) {
            uninvestedAmount = amountForInvest % investedAmount;
        }
        report.setUnnasignedAmount(uninvestedAmount);
        report.calculatePercentage();
        
        return report;
    }

    private boolean isMandatoryFundTypesPresent(List<InvestmentFund> investmentFunds) {
        Set<FundType> fundTypesForDivide = getPercentages().keySet();

        Set<FundType> givenFundTypes = investmentFunds
                .stream()
                .map(e -> e.getFundType())
                .collect(Collectors.toSet());

        return fundTypesForDivide.containsAll(givenFundTypes) && givenFundTypes.containsAll(fundTypesForDivide);
    }

    public abstract Map<FundType, Integer> getPercentages();
}
