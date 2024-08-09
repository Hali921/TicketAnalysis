import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TicketAnalysis {

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/tickets.json"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }

            String fileContent = sb.toString();
            if (fileContent.startsWith("\uFEFF")) {
                fileContent = fileContent.substring(1);
            }

            JSONObject json = new JSONObject(fileContent);
            JSONArray tickets = json.getJSONArray("tickets");

            Map<String, List<Long>> flightTimes = new HashMap<>();
            List<Integer> prices = new ArrayList<>();

            for (int i = 0; i < tickets.length(); i++) {
                JSONObject ticket = tickets.getJSONObject(i);

                if (ticket.getString("origin").equals("VVO") && ticket.getString("destination").equals("TLV")) {
                    String carrier = ticket.getString("carrier");
                    long flightTime = calculateFlightTime(
                            ticket.getString("departure_date") + " " + ticket.getString("departure_time"),
                            ticket.getString("arrival_date") + " " + ticket.getString("arrival_time")
                    );

                    flightTimes.putIfAbsent(carrier, new ArrayList<>());
                    flightTimes.get(carrier).add(flightTime);

                    prices.add(ticket.getInt("price"));
                }
            }

            System.out.println("Минимальное время полета для каждого авиаперевозчика:");
            for (Map.Entry<String, List<Long>> entry : flightTimes.entrySet()) {
                long minTime = Collections.min(entry.getValue());
                System.out.printf("%s: %d минут\n", entry.getKey(), minTime);
            }

            double averagePrice = calculateAverage(prices);
            double medianPrice = calculateMedian(prices);
            double priceDifference = averagePrice - medianPrice;

            System.out.printf("Средняя цена: %.2f\n", averagePrice);
            System.out.printf("Медиана цены: %.2f\n", medianPrice);
            System.out.printf("Разница между средней ценой и медианой: %.2f\n", priceDifference);

        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        } catch (org.json.JSONException e) {
            System.err.println("Ошибка обработки JSON данных: " + e.getMessage());
        } catch (ParseException e) {
            System.err.println("Ошибка обработки даты: " + e.getMessage());
        }
    }

    // Метод для расчета времени полета в минутах
    private static long calculateFlightTime(String departure, String arrival) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm");
        Date departureDate = format.parse(departure);
        Date arrivalDate = format.parse(arrival);

        return (arrivalDate.getTime() - departureDate.getTime()) / (1000 * 60); // время в минутах
    }

    private static double calculateAverage(List<Integer> prices) {
        return prices.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    private static double calculateMedian(List<Integer> prices) {
        Collections.sort(prices);
        int size = prices.size();
        if (size % 2 == 0) {
            return (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
        } else {
            return prices.get(size / 2);
        }
    }
}
