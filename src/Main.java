import java.io.*;
import java.util.concurrent.*;

class Main {
    public static void main(String[] args) {
        File sourceFile = new File("source.txt");
        File consumer1File = new File("consumer1.txt");
        File consumer2File = new File("consumer2.txt");

        BlockingQueue<Integer> sourceQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Integer> consumer1Queue = new LinkedBlockingQueue<>();
        BlockingQueue<Integer> consumer2Queue = new LinkedBlockingQueue<>();

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Читаем числа из файла источника в очередь sourceQueue
        executor.execute(new SourceReader(sourceFile, sourceQueue));

        // Потребители обрабатывают числа из sourceQueue и записывают результат в соответствующие файлы
        executor.execute(new Consumer1(consumer1Queue, consumer1File, "Потребитель 1"));
        executor.execute(new Consumer2(consumer2Queue, consumer2File, "Потребитель 2"));

        // Копируем числа из sourceQueue в очереди потребителей
        executor.execute(new NumberCopier(sourceQueue, consumer1Queue, consumer2Queue));

        executor.shutdown();
        var y = 4;
    }
}

class SourceReader implements Runnable {
    private final File sourceFile;
    private final BlockingQueue<Integer> queue;

    public SourceReader(File sourceFile, BlockingQueue<Integer> queue) {
        this.sourceFile = sourceFile;
        this.queue = queue;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int number = Integer.parseInt(line);
                queue.put(number);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Consumer1 implements Runnable {
    private final BlockingQueue<Integer> sourceQueue;
    private final File outputFile;
    private final String name;

    public Consumer1(BlockingQueue<Integer> sourceQueue, File outputFile, String name) {
        this.sourceQueue = sourceQueue;
        this.outputFile = outputFile;
        this.name = name;
    }

    @Override
    public void run() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            while (!Thread.currentThread().isInterrupted()) {
                Integer number = sourceQueue.poll(100, TimeUnit.MILLISECONDS); // Poll with timeout
                if (number != null) {
                    // Perform operations on the number (e.g., square it)
                    int result = number * number;
                    writer.println(name + ": " + result);
                    writer.flush();
                } else {
                    break; // Break the loop if no more elements are available
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
    }
}

class Consumer2 implements Runnable {
    private final BlockingQueue<Integer> sourceQueue;
    private final File outputFile;
    private final String name;

    public Consumer2(BlockingQueue<Integer> sourceQueue, File outputFile, String name) {
        this.sourceQueue = sourceQueue;
        this.outputFile = outputFile;
        this.name = name;
    }

    @Override
    public void run() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            while (!Thread.currentThread().isInterrupted()) {
                Integer number = sourceQueue.poll(100, TimeUnit.MILLISECONDS); // Poll with timeout
                if (number != null) {
                    // Perform operations on the number (e.g., cube it)
                    int result = number * number * number;
                    writer.println(name + ": " + result);
                    writer.flush();
                } else {
                    break; // Break the loop if no more elements are available
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
    }
}

class NumberCopier implements Runnable {
    private final BlockingQueue<Integer> sourceQueue;
    private final BlockingQueue<Integer> consumer1Queue;
    private final BlockingQueue<Integer> consumer2Queue;

    public NumberCopier(BlockingQueue<Integer> sourceQueue, BlockingQueue<Integer> consumer1Queue, BlockingQueue<Integer> consumer2Queue) {
        this.sourceQueue = sourceQueue;
        this.consumer1Queue = consumer1Queue;
        this.consumer2Queue = consumer2Queue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Integer number = sourceQueue.poll(100, TimeUnit.MILLISECONDS); // Poll with timeout
                if (number != null) {
                    consumer1Queue.put(number);
                    consumer2Queue.put(number);
                } else {
                    break; // Break the loop if no more elements are available
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        }
    }
}

