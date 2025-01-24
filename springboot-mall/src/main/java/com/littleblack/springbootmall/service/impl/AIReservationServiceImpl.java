package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.dao.ConversationMessageDao;
import com.littleblack.springbootmall.model.ConversationMessage;
import com.littleblack.springbootmall.model.Reservation;
import com.littleblack.springbootmall.model.User;
import com.littleblack.springbootmall.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class AIReservationServiceImpl implements AIReservationService {

    private static final String DATE_FORMAT = "yyyy/MM/dd";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{8}$");

    @Autowired
    private ConversationMessageDao conversationMessageDao;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private UserService userService;

    private final Map<String, Map<String, String>> reservationData = new HashMap<>();
    private final Map<String, Map<String, String>> queryData = new HashMap<>();

    @Override
    public String handleReservationIntent(Integer userId, String conversationId, String userMessage) {
        if (userId == null) {
            return "請先登入後再進行訂位。";
        }

        String newConversationId = conversationId != null ? conversationId : UUID.randomUUID().toString();
        reservationData.put(newConversationId, new HashMap<>());

        ConversationMessage message = new ConversationMessage();
        message.setUserId(userId);
        message.setConversationId(newConversationId);
        message.setContent(userMessage);
        message.setRole("user");
        message.setConversationType("RESERVATION");
        message.setReservationStage("DATE");

        conversationMessageDao.createMessage(message);

        return "好的，讓我幫您安排訂位。請問您想要預約哪一天用餐？請以 YYYY/MM/DD 的格式輸入日期，例如：2024/02/25";
    }

    @Override
    public String processReservationInfo(Integer userId, String conversationId, String userMessage) {
        ConversationMessage lastMessage = conversationMessageDao.getLatestMessageByConversationId(conversationId);
        if (lastMessage == null) {
            return "對不起，找不到您的預約記錄。請重新開始預約流程。";
        }

        String currentStage = lastMessage.getReservationStage();
        Map<String, String> currentReservation = reservationData.computeIfAbsent(conversationId, k -> new HashMap<>());

        switch (currentStage) {
            case "DATE":
                return handleDateInput(userId, conversationId, userMessage, currentReservation);
            case "TIME_SLOT":
                return handleTimeSlotInput(userId, conversationId, userMessage, currentReservation);
            case "GUEST_COUNT":
                return handleGuestCountInput(userId, conversationId, userMessage, currentReservation);
            case "NAME":
                return handleNameInput(userId, conversationId, userMessage, currentReservation);
            case "PHONE":
                return handlePhoneInput(userId, conversationId, userMessage, currentReservation);
            case "SPECIAL_REQUEST":
                return handleSpecialRequestInput(userId, conversationId, userMessage, currentReservation);
            default:
                return "對不起，發生了錯誤。請重新開始預約流程。";
        }
    }

    private String handleDateInput(Integer userId, String conversationId, String userMessage,
                                   Map<String, String> reservationInfo) {
        try {
            LocalDate date = LocalDate.parse(userMessage, DateTimeFormatter.ofPattern(DATE_FORMAT));

            if (date.isBefore(LocalDate.now())) {
                return "抱歉，無法預約過去的日期。請重新輸入有效日期（YYYY/MM/DD）";
            }

            List<String> availableTimeRanges = timeSlotService.getAvailableTimeRanges(date);
            if (availableTimeRanges.isEmpty()) {
                return "抱歉，該日期已無可用時段。請選擇其他日期。";
            }

            reservationInfo.put("date", userMessage);
            updateConversationStage(userId, conversationId, "TIME_SLOT");

            StringBuilder response = new StringBuilder("請選擇用餐時段：");
            for (String timeRange : availableTimeRanges) {
                response.append(" ").append(timeRange);
            }
            response.append("\n請直接輸入時段（例如：18:00-19:00）");

            return response.toString();
        } catch (DateTimeParseException e) {
            return "日期格式不正確，請使用 YYYY/MM/DD 格式（例如：2024/02/25）";
        }
    }

    private String handleTimeSlotInput(Integer userId, String conversationId, String userMessage,
                                       Map<String, String> reservationInfo) {
        if (!isValidTimeSlot(userMessage)) {
            return "請選擇有效的時段格式（例如：18:00-19:00）";
        }

        LocalDate date = LocalDate.parse(reservationInfo.get("date"), DateTimeFormatter.ofPattern(DATE_FORMAT));
        if (!timeSlotService.getAvailableTimeRanges(date).contains(userMessage)) {
            return "抱歉，該時段已無法預約，請選擇其他時段。";
        }

        reservationInfo.put("time", userMessage);
        updateConversationStage(userId, conversationId, "GUEST_COUNT");

        return "請問用餐人數？（請輸入1-30之間的數字）";
    }

    private String handleGuestCountInput(Integer userId, String conversationId, String userMessage,
                                         Map<String, String> reservationInfo) {
        try {
            int guestCount = Integer.parseInt(userMessage);
            if (guestCount < 1 || guestCount > 30) {
                return "人數必須在1-30之間，請重新輸入";
            }

            LocalDate date = LocalDate.parse(reservationInfo.get("date"), DateTimeFormatter.ofPattern(DATE_FORMAT));
            Integer timeSlotId = timeSlotService.getTimeSlotIdByTime(reservationInfo.get("time"));

            if (!timeSlotService.isSlotAvailable(date, timeSlotId, guestCount)) {
                return "抱歉，該時段已無法容納 " + guestCount + " 人，請選擇其他時段或減少人數。";
            }

            reservationInfo.put("guestCount", userMessage);
            updateConversationStage(userId, conversationId, "NAME");

            return "請問訂位人姓名？";
        } catch (NumberFormatException e) {
            return "請輸入有效的數字（1-30之間）";
        }
    }

    private String handleNameInput(Integer userId, String conversationId, String userMessage,
                                   Map<String, String> reservationInfo) {
        if (userMessage.trim().isEmpty()) {
            return "姓名不能為空，請重新輸入";
        }

        reservationInfo.put("name", userMessage.trim());
        updateConversationStage(userId, conversationId, "PHONE");

        return "請輸入聯絡電話（格式：0912345678）";
    }

    private String handlePhoneInput(Integer userId, String conversationId, String userMessage,
                                    Map<String, String> reservationInfo) {
        if (!PHONE_PATTERN.matcher(userMessage).matches()) {
            return "電話格式不正確，請輸入正確的手機號碼（例如：0912345678）";
        }

        reservationInfo.put("phone", userMessage);

        User user = userService.getUserById(userId);
        if (user == null || user.getEmail() == null) {
            return "無法獲取用戶信息，請重新登入後再試。";
        }
        reservationInfo.put("email", user.getEmail());

        updateConversationStage(userId, conversationId, "SPECIAL_REQUEST");

        return "是否有特殊需求？（如果沒有，請輸入「無」）";
    }

    private String handleSpecialRequestInput(Integer userId, String conversationId, String userMessage,
                                             Map<String, String> reservationInfo) {
        try {
            reservationInfo.put("specialRequests", userMessage.equals("無") ? "" : userMessage);

            Reservation reservation = createReservationFromInfo(userId, reservationInfo);
            String reservationId = reservationService.createReservation(reservation);

            updateConversationStage(userId, conversationId, "COMPLETED");
            reservationData.remove(conversationId);

            return formatReservationConfirmation(reservationId, reservationInfo);
        } catch (Exception e) {
            return "預約過程中發生錯誤，請稍後重試或聯繫客服人員。";
        }
    }

    @Override
    public String handleReservationQuery(Integer userId, String userMessage) {
        String conversationId = UUID.randomUUID().toString();
        ConversationMessage message = new ConversationMessage();
        message.setUserId(userId);
        message.setConversationId(conversationId);
        message.setContent(userMessage);
        message.setRole("user");
        message.setConversationType("QUERY");
        message.setReservationStage("NAME");

        conversationMessageDao.createMessage(message);

        return "請輸入訂位人姓名：";
    }

    @Override
    public String processReservationQuery(Integer userId, String conversationId, String userMessage) {
        ConversationMessage lastMessage = conversationMessageDao.getLatestMessageByConversationId(conversationId);
        if (lastMessage == null) {
            return "對不起，查詢過程發生錯誤，請重新開始查詢。";
        }

        String currentStage = lastMessage.getReservationStage();
        Map<String, String> queryInfo = queryData.computeIfAbsent(conversationId, k -> new HashMap<>());

        switch (currentStage) {
            case "NAME":
                if (userMessage.trim().isEmpty()) {
                    return "姓名不能為空，請重新輸入訂位人姓名：";
                }
                queryInfo.put("name", userMessage.trim());
                updateConversationStage(userId, conversationId, "PHONE");
                return "請輸入聯絡電話（格式：0912345678）：";

            case "PHONE":
                if (!PHONE_PATTERN.matcher(userMessage).matches()) {
                    return "電話格式不正確，請輸入正確的手機號碼（例如：0912345678）：";
                }
                queryInfo.put("phone", userMessage);

                List<Reservation> reservations = reservationService.queryReservations(
                        null, null, null,
                        queryInfo.get("name"),
                        queryInfo.get("phone"),
                        null, null
                );

                if (reservations.isEmpty()) {
                    updateConversationStage(userId, conversationId, "NAME");
                    return "找不到符合的訂位記錄，請確認姓名和電話是否正確。\n請重新輸入訂位人姓名：";
                }

                StringBuilder result = new StringBuilder("找到以下訂位記錄：\n\n");
                for (Reservation reservation : reservations) {
                    result.append(formatReservationInfo(reservation)).append("\n\n");
                }

                updateConversationStage(userId, conversationId, "COMPLETED");
                queryData.remove(conversationId);
                return result.toString();

            default:
                return "查詢過程發生錯誤，請重新開始查詢。";
        }
    }

    @Override
    public String handleReservationCancel(Integer userId, String message) {
        ConversationMessage latestState = getLatestConversationState(userId);
        if (latestState != null && "CANCEL".equals(latestState.getConversationType())) {
            return processCancellationInfo(userId, latestState.getConversationId(), message);
        }

        String newConversationId = UUID.randomUUID().toString();
        ConversationMessage newMessage = new ConversationMessage();
        newMessage.setUserId(userId);
        newMessage.setConversationId(newConversationId);
        newMessage.setContent(message);
        newMessage.setRole("user");
        newMessage.setConversationType("CANCEL");
        newMessage.setReservationStage("NAME");

        conversationMessageDao.createMessage(newMessage);

        return "請輸入訂位人姓名：";
    }

    private String processCancellationInfo(Integer userId, String conversationId, String userInput) {
        ConversationMessage lastMessage = conversationMessageDao.getLatestMessageByConversationId(conversationId);
        if (lastMessage == null) {
            return "抱歉，找不到取消訂位的記錄。請重新開始取消流程。";
        }

        Map<String, String> cancelInfo = new HashMap<>();
        List<ConversationMessage> previousMessages = conversationMessageDao.getMessagesByConversationId(conversationId);
        for (ConversationMessage msg : previousMessages) {
            if ("user".equals(msg.getRole()) && !msg.getContent().equals(userInput)) {
                if ("NAME".equals(msg.getReservationStage())) {
                    cancelInfo.put("name", msg.getContent());
                } else if ("PHONE".equals(msg.getReservationStage())) {
                    cancelInfo.put("phone", msg.getContent());
                }
            }
        }

        String currentStage = lastMessage.getReservationStage();
        switch (currentStage) {
            case "NAME":
                if (userInput.trim().isEmpty()) {
                    return "姓名不能為空，請重新輸入：";
                }
                updateConversationStage(userId, conversationId, "PHONE");
                return "請輸入聯絡電話（格式：0912345678）：";

            case "PHONE":
                if (!PHONE_PATTERN.matcher(userInput).matches()) {
                    return "電話格式不正確，請輸入正確的手機號碼（例如：0912345678）：";
                }
                cancelInfo.put("phone", userInput);

                List<Reservation> reservations = reservationService.queryReservations(
                        null, null, null,
                        cancelInfo.get("name"),
                        userInput, null, null
                );

                if (reservations.isEmpty()) {
                    updateConversationStage(userId, conversationId, "NAME");
                    return "找不到符合的訂位記錄，請重新確認姓名和電話。\n請重新輸入訂位人姓名：";
                }

                if (reservations.size() == 1) {
                    Reservation reservation = reservations.get(0);
                    boolean cancelled = reservationService.cancelReservationByCustomerInfo(
                            reservation.getReservationId(),
                            cancelInfo.get("name"),
                            userInput,
                            "由客人透過AI客服取消"
                    );

                    if (cancelled) {
                        updateConversationStage(userId, conversationId, "COMPLETED");
                        return String.format(
                                "訂位已成功取消！\n取消的訂位資訊：\n日期：%s\n時段：%s\n人數：%d人\n\n取消確認信已寄送至您的信箱。",
                                reservation.getReservationDate(),
                                getTimeSlotString(reservation.getTimeSlotId()),
                                reservation.getGuestCount()
                        );
                    } else {
                        return "無法取消訂位。可能是已超過取消期限（用餐時間24小時前）或訂位已被取消。";
                    }
                } else {
                    StringBuilder response = new StringBuilder("找到多筆訂位記錄，請選擇要取消的訂位：\n");
                    for (int i = 0; i < reservations.size(); i++) {
                        Reservation r = reservations.get(i);
                        response.append(String.format("%d. 日期：%s，時段：%s，人數：%d人\n",
                                i + 1,
                                r.getReservationDate(),
                                getTimeSlotString(r.getTimeSlotId()),
                                r.getGuestCount()
                        ));
                    }
                    updateConversationStage(userId, conversationId, "SELECT_RESERVATION");
                    return response.toString();
                }

            case "SELECT_RESERVATION":
                try {
                    int selection = Integer.parseInt(userInput);
                    List<Reservation> allReservations = reservationService.queryReservations(
                            null, null, null,
                            cancelInfo.get("name"),
                            cancelInfo.get("phone"),
                            null, null
                    );

                    if (selection < 1 || selection > allReservations.size()) {
                        return "請輸入有效的選項編號：";
                    }

                    Reservation selectedReservation = allReservations.get(selection - 1);
                    boolean cancelled = reservationService.cancelReservationByCustomerInfo(
                            selectedReservation.getReservationId(),
                            cancelInfo.get("name"),
                            cancelInfo.get("phone"),
                            "由客人透過AI客服取消"
                    );

                    if (cancelled) {
                        updateConversationStage(userId, conversationId, "COMPLETED");
                        return String.format(
                                "訂位已成功取消！\n取消的訂位資訊：\n日期：%s\n時段：%s\n人數：%d人\n\n取消確認信已寄送至您的信箱。",
                                selectedReservation.getReservationDate(),
                                getTimeSlotString(selectedReservation.getTimeSlotId()),
                                selectedReservation.getGuestCount()
                        );
                    } else {
                        return "無法取消訂位。可能是已超過取消期限（用餐時間24小時前）或訂位已被取消。";
                    }
                } catch (NumberFormatException e) {
                    return "請輸入有效的數字：";
                }

            default:
                return "抱歉，系統發生錯誤。請重新開始取消流程。";
        }
    }

    private void updateConversationStage(Integer userId, String conversationId, String stage) {
        ConversationMessage message = new ConversationMessage();
        message.setUserId(userId);
        message.setConversationId(conversationId);
        message.setContent("Stage update: " + stage);
        message.setRole("system");
        message.setConversationType("RESERVATION");
        message.setReservationStage(stage);

        conversationMessageDao.createMessage(message);
    }

    private boolean isValidTimeSlot(String timeSlot) {
        return timeSlot.matches("(17:00-18:00|18:00-19:00|19:00-20:00|20:00-21:00|21:00-22:00)");
    }

    private String formatReservationConfirmation(String reservationId, Map<String, String> info) {
        return String.format("""
                訂位成功！
                訂位編號：%s
                
                訂位詳情：
                日期：%s
                時間：%s
                人數：%s人
                姓名：%s
                電話：%s
                
                確認信已寄送至您的信箱（%s）
                如需修改或取消訂位，請提供訂位編號與聯絡電話。
                """,
                reservationId,
                info.get("date"),
                info.get("time"),
                info.get("guestCount"),
                info.get("name"),
                info.get("phone"),
                info.get("email"));
    }

    private String formatReservationInfo(Reservation reservation) {
        return String.format("""
                訂位資訊：
                預約日期：%s
                用餐時段：%s
                用餐人數：%d人
                訂位狀態：%s
                訂位人：%s
                聯絡電話：%s
                特殊需求：%s""",
                reservation.getReservationDate(),
                getTimeSlotString(reservation.getTimeSlotId()),
                reservation.getGuestCount(),
                getStatusText(reservation.getStatus()),
                reservation.getCustomerName(),
                reservation.getContactPhone(),
                reservation.getSpecialRequests() != null ? reservation.getSpecialRequests() : "無"
        );
    }

    private Reservation createReservationFromInfo(Integer userId, Map<String, String> info) {
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setCustomerName(info.get("name"));

        LocalDate localDate = LocalDate.parse(info.get("date"), DateTimeFormatter.ofPattern(DATE_FORMAT));
        reservation.setReservationDate(Date.valueOf(localDate));
        reservation.setTimeSlotId(timeSlotService.getTimeSlotIdByTime(info.get("time")));
        reservation.setGuestCount(Integer.parseInt(info.get("guestCount")));
        reservation.setContactPhone(info.get("phone"));
        reservation.setEmail(info.get("email"));
        reservation.setSpecialRequests(info.get("specialRequests"));
        reservation.setSource("AI");
        reservation.setStatus(0);

        LocalDateTime cancelDeadline = localDate.atTime(0, 0).minusHours(24);
        reservation.setCancelDeadline(cancelDeadline);

        return reservation;
    }

    @Override
    public boolean isReservationIntent(String message) {
        String lowerMsg = message.toLowerCase();
        return lowerMsg.contains("訂位") ||
                lowerMsg.contains("預約") ||
                lowerMsg.contains("訂餐") ||
                lowerMsg.contains("訂桌") ||
                lowerMsg.contains("預訂");
    }

    @Override
    public boolean isReservationQuery(String message) {
        String lowerMsg = message.toLowerCase();
        return lowerMsg.contains("查詢") ||
                lowerMsg.contains("確認") ||
                lowerMsg.contains("訂位狀態") ||
                lowerMsg.contains("預約狀態") ||
                lowerMsg.contains("查看訂位");
    }

    @Override
    public boolean isReservationCancel(String message) {
        String lowerMsg = message.toLowerCase();
        return lowerMsg.contains("取消") ||
                lowerMsg.contains("取消訂位") ||
                lowerMsg.contains("取消預約") ||
                lowerMsg.contains("不要訂位了");
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待確認";
            case 1 -> "已確認";
            case 2 -> "已取消";
            default -> "未知狀態";
        };
    }

    private String getTimeSlotString(Integer timeSlotId) {
        Map<Integer, String> timeSlots = new HashMap<>();
        timeSlots.put(1, "17:00-18:00");
        timeSlots.put(2, "18:00-19:00");
        timeSlots.put(3, "19:00-20:00");
        timeSlots.put(4, "20:00-21:00");
        timeSlots.put(5, "21:00-22:00");
        return timeSlots.getOrDefault(timeSlotId, "未知時段");
    }

    @Override
    public ConversationMessage getLatestConversationState(Integer userId) {
        return conversationMessageDao.getLatestMessageByUserId(userId);
    }
}