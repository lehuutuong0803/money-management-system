package com.tiuon.moneymanager.service.impl;

import com.tiuon.moneymanager.dto.ExpenseDto;
import com.tiuon.moneymanager.entity.ProfileEntity;
import com.tiuon.moneymanager.repository.ProfileRepository;
import com.tiuon.moneymanager.service.IEmailService;
import com.tiuon.moneymanager.service.IExpenseService;
import com.tiuon.moneymanager.service.INotificationService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements INotificationService {

    private final ProfileRepository profileRepository;
    private final IEmailService iEmailService;
    private final IExpenseService iExpenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "Europe/Berlin")
//    @Scheduled(cron = "0 * * * * *", zone = "Europe/Berlin")
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder");
        List<ProfileEntity> profileEntities = profileRepository.findAll();
        for(ProfileEntity profile : profileEntities) {
            String body = "Hi " + profile.getFullName() + ", <br></br>"
                    + "This is a friendly reminder to add your incomes and expenses for today in Money Management.<br></br>"
                    + "<a href="+frontendUrl+" style='display:inline-block;padding:10px 20px;background-color:#4CAF50;color:#fff;text-decoration:none;border-radius:5px;font-weight:bold;'>Go to Money Manager</a>"
                    + "<br><bn>Best regards, <br>Money Manager Team";
            iEmailService.sendEmail(profile.getEmail(),"Daily reminder: Add your income and expenses", body);
        }
        log.info("Job Completed: sendDailyIncomeExpenseReminder");
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "Europe/Berlin")
//    @Scheduled(cron = "0 * * * * *", zone = "Europe/Berlin")
    public void sendDailyExpenseSummary() {
        log.info("Job started: sendDailyExpenseSummary");
        List<ProfileEntity> profileEntityList = profileRepository.findAll();
        for ( ProfileEntity profile : profileEntityList) {
            List<ExpenseDto> expenseDtoList = iExpenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now(ZoneId.of("Europe/Berlin")));
            if (!expenseDtoList.isEmpty()) {
                StringBuilder html = new StringBuilder();
                html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
                html.append("<thead>");
                html.append("<tr>");
                html.append("<th style='padding: 8px; text-align: left; background-color: #f2f2f2;'>S. No</th>");
                html.append("<th style='padding: 8px; text-align: left; background-color: #f2f2f2;'>Name</th>");
                html.append("<th style='padding: 8px; text-align: left; background-color: #f2f2f2;'>Category</th>");
                html.append("<th style='padding: 8px; text-align: left; background-color: #f2f2f2;'>Amount</th>");
                html.append("<th style='padding: 8px; text-align: left; background-color: #f2f2f2;'>Date</th>");
                html.append("</tr>");
                html.append("</thead>");
                html.append("<tbody>");

                // Example row - repeat this for each data item
                int i = 1;
                for(ExpenseDto expenseDto : expenseDtoList) {
                    html.append("<tr>");
                    html.append("<td style='padding: 8px;'>").append(i).append("</td>");
                    html.append("<td style='padding: 8px;'>").append(expenseDto.getName()).append("</td>");
                    html.append("<td style='padding: 8px;'>").append(expenseDto.getCategoryId() != null ? expenseDto.getCategoryName() : "N/A").append("</td>");
                    html.append("<td style='padding: 8px;'>").append(expenseDto.getAmount()).append("</td>");
                    html.append("<td style='padding: 8px;'>").append(expenseDto.getDate()).append("</td>");
                    html.append("</tr>");
                }
                html.append("</tbody>");
                html.append("</table>");
                String body = "Hi " +profile.getFullName() + ", <br/><br/> Here is a summary of your expenses for today: <br/><br/>"
                        + html + "<br/><br/>Best regards, <br/>Money Manager Team";
                iEmailService.sendEmail(profile.getEmail(), "You daily Expense Summany", body);
            }
        }

        log.info("Job Completed: sendDailyExpenseSummary");
    }
}
