package producer;

import SharedBuffer.SharedBuffer;
import model.Transaction;
import util.CsvParser;

import java.io.*;

public class CsvProducer implements Runnable {
    private final SharedBuffer<Transaction> buffer;
    private final String filePath;

    public CsvProducer(SharedBuffer<Transaction> buffer, String filePath) {
        this.buffer = buffer;
        this.filePath = filePath;
    }

    CsvParser csvParser = new CsvParser();

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip header row
                }
                Transaction transaction = csvParser.parse(line);
                buffer.add(transaction);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
