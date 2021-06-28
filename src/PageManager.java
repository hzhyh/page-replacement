import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

// 20210616
public class PageManager {
    private static Scanner scanner = new Scanner(System.in); // 用來接受使用者的輸入
    private String filename;
    private int frameCount; // number of page frames
    private int[] references; // page references
    private PrintWriter writer = null;

    public static void main(String[] args) {

        // 印出作者與題目資訊
        opening();
        
        while (true) {
            // 開始一個新的排序
            PageManager manager = new PageManager();
            
            // 讀檔
            manager.read();

            // 執行模擬並輸出
            manager.run();
            
            // 決定是否要開始新的一輪
            if (ending()) {
                break;
            }
        }
        
        System.out.println("Bye!");
    }
    
    private static void opening() {
        System.out.println("1092 作業系統 作業三");
        System.out.println();
        System.out.println("題目簡介：");
        System.out.println("給定一檔案內有Page Frame個數，以及各個Page Reference的次序，\n"
                         + "請依這些資訊模擬各種指定的Page Replacement方法。\n\n"
                         + "輸出結果必須繪出每次Page Reference時，每一個Page Frame記錄的內容，\n"
                         + "並計算每種方法之Page Fault次數以及Page Replace次數。\n\n"
                         + "程式須實現以下 Page Replacement 方法\n"
                         + "1.First In First Out(FIFO)\n"
                         + "2.Least Recently Used (LRU)\n"
                         + "3.Least Frequently Used (LFU) + FIFO\n"
                         + "4.Most Frequently Used (MFU) + FIFO\n"
                         + "5.LFU + LRU\n"
                         + "6.MFU + LRU");
        System.out.println();
        System.out.println("Let's get started!");
        System.out.println();
    }

    private static boolean ending() {
        System.out.print("\nDo you have another input file? (Y/N): ");
        char c = scanner.next().charAt(0);
        if (c != 'y' && c != 'Y') {
            return true;
        }
        return false;
    }
    
    private void read() {
        // 確認input資料夾存在
        File file = new File("input");
        while (!file.exists() || !file.isDirectory()) {
            System.out.println("Please put all input files in PageReplacement/input/");
            System.out.println("Done? Press ENTER to continue...");
            scanner.nextLine();
        }
        
        // 輸入檔名，取得檔案
        System.out.print("Input filename: ");
        filename = scanner.next();
        file = new File("input", filename);
        
        // 若檔案不存在，要求重新輸入直到取得存在的檔案
        while (!file.exists()) {
            System.out.println("File not found! Check up and input again.");
            System.out.print("Input filename: ");
            filename = scanner.next();
            file = new File("input", filename);
        }
        
        // 讀取檔案內容
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        frameCount = fileScanner.nextInt();
        System.out.println("page frame count : " + frameCount);
        
        String str = fileScanner.next();
        references = new int[str.length()];
        System.out.print("page references :");
        for (int i = 0; i < str.length(); i++) {
            references[i] = str.charAt(i) - '0';
            System.out.print(" " + references[i]);
        }
        System.out.println();
        
        fileScanner.close();
    }
    
    private void run() {
        // 建立輸出用的 writer
        createWriter();
        // 模擬
        fifo();
        lru();
        lfuFifo();
        mfuFifo();
        lfuLru();
        mfuLru();
        // 關閉 writer
        writer.close();
    }
    
    private void createWriter() {
        // 創建output資料夾如果它不存在
        File file = new File("output");
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
        
        // 弄出output檔名
        filename = "out_" + filename;
        
        // 建立writer
        try {
            writer = new PrintWriter(new File("output", filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private void write(Object o) {
        writer.print(o);
    }
    
    private void writeln() {
        writer.println();
    }
    
    private void writeln(Object o) {
        writer.println(o);
    }

    private void fifo() {
        ArrayList<Integer> pages = new ArrayList<Integer>(); // page frames
        int faultCount = 0;
        int replaceCount = 0;
        
        writeln("--------------FIFO-----------------------");
        
        for (int i = 0; i < references.length; i++) {
            int page = references[i]; // 要 reference 的 page
            
            // 在 page frames 中尋找此 page
            boolean found = false;
            for (int j = 0; j < pages.size(); j++ ) {
                if (pages.get(j) == page) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                faultCount++;
                if (pages.size() < frameCount) {
                    pages.add(page);
                } else {
                    replaceCount++;
                    pages.remove(0);
                    pages.add(page);
                }
            }
            
            write(page + "\t");
            for (int j = pages.size()-1; j >= 0; j--) {
                write(pages.get(j));
            }
            if (!found) {
                write("\tF");
            }
            writeln();
        }

        write("Page Fault = " + faultCount);
        write("  Page Replaces = " + replaceCount);
        writeln("  Page Frames = " + frameCount);
    }

    private void lru() {
        ArrayList<Integer> pages = new ArrayList<Integer>(); // page frames
        int faultCount = 0;
        int replaceCount = 0;
        
        writeln("\n--------------LRU-----------------------");

        for (int i = 0; i < references.length; i++) {
            int page = references[i]; // 要 reference 的 page
            
            // 在 page frames 中尋找此 page
            boolean found = false;
            for (int j = 0; j < pages.size(); j++ ) {
                if (pages.get(j) == page) {
                    found = true;
                    pages.remove(j);
                    pages.add(page);
                    break;
                }
            }
            
            if (!found) {
                faultCount++;
                if (pages.size() < frameCount) {
                    pages.add(page);
                } else {
                    replaceCount++;
                    pages.remove(0);
                    pages.add(page);
                }
            }
            
            write(page + "\t");
            for (int j = pages.size()-1; j >= 0; j--) {
                write(pages.get(j));
            }
            if (!found) {
                write("\tF");
            }
            writeln();
        }

        write("Page Fault = " + faultCount);
        write("  Page Replaces = " + replaceCount);
        writeln("  Page Frames = " + frameCount);
    }

    private void lfuFifo() {
        ArrayList<Integer> pages = new ArrayList<Integer>(); // page frames
        int faultCount = 0;
        int replaceCount = 0;
        
        writeln("\n--------------Least Frequently Used Page Replacement-----------------------");

        // 用個位數表示頁碼；十位數以上表示使用次數-1
        // 27代表7這頁使用過3次
        for (int i = 0; i < references.length; i++) {
            int page = references[i]; // 要 reference 的 page
            
            // 在 page frames 中尋找此 page
            boolean found = false;
            for (int j = 0; j < pages.size(); j++ ) {
                if ((pages.get(j) % 10) == page) {
                    found = true;
                    pages.set(j, pages.get(j)+10); // 次數+1
                    break;
                }
            }
            
            if (!found) {
                faultCount++;
                if (pages.size() < frameCount) { // 還有空的頁框
                    pages.add(page);
                } else { // 頁置換
                    replaceCount++;
                    // 先找到最小的次數
                    int least = pages.get(0) / 10;
                    for (int j = 1; j < pages.size(); j++) {
                        if (pages.get(j) / 10 < least) {
                            least = pages.get(j) / 10;
                        }
                    }
                    // 再置換次數最小者
                    for (int j = 0; j < pages.size(); j++) {
                        if (pages.get(j) / 10 == least) {
                            pages.remove(j);
                            pages.add(page);
                            break;
                        }
                    }
                }
            }
            
            write(page + "\t");
            for (int j = pages.size()-1; j >= 0; j--) {
                write(pages.get(j) % 10);
            }
            if (!found) {
                write("\tF");
            }
            writeln();
        }

        write("Page Fault = " + faultCount);
        write("  Page Replaces = " + replaceCount);
        writeln("  Page Frames = " + frameCount);
    }

    private void mfuFifo() {
        ArrayList<Integer> pages = new ArrayList<Integer>(); // page frames
        int faultCount = 0;
        int replaceCount = 0;
        
        writeln("\n--------------Most Frequently Used Page Replacement -----------------------");

        // 用個位數表示頁碼；十位數以上表示使用次數-1
        // 27代表7這頁使用過3次
        for (int i = 0; i < references.length; i++) {
            int page = references[i]; // 要 reference 的 page
            
            // 在 page frames 中尋找此 page
            boolean found = false;
            for (int j = 0; j < pages.size(); j++ ) {
                if ((pages.get(j) % 10) == page) {
                    found = true;
                    pages.set(j, pages.get(j)+10); // 次數+1
                    break;
                }
            }
            
            if (!found) {
                faultCount++;
                if (pages.size() < frameCount) { // 還有空的頁框
                    pages.add(page);
                } else { // 頁置換
                    replaceCount++;
                    // 先找到最大的次數
                    int max = 0;
                    for (int j = 0; j < pages.size(); j++) {
                        if (pages.get(j) / 10 > max) {
                            max = pages.get(j) / 10;
                        }
                    }
                    // 再置換次數最大者
                    for (int j = 0; j < pages.size(); j++) {
                        if (pages.get(j) / 10 == max) {
                            pages.remove(j);
                            pages.add(page);
                            break;
                        }
                    }
                }
            }
            
            write(page + "\t");
            for (int j = pages.size()-1; j >= 0; j--) {
                write(pages.get(j) % 10);
            }
            if (!found) {
                write("\tF");
            }
            writeln();
        }

        write("Page Fault = " + faultCount);
        write("  Page Replaces = " + replaceCount);
        writeln("  Page Frames = " + frameCount);
    }

    private void lfuLru() {
        ArrayList<Integer> pages = new ArrayList<Integer>(); // page frames
        int faultCount = 0;
        int replaceCount = 0;
        
        writeln("--------------Least Frequently Used LRU Page Replacement-----------------------");

        // 用個位數表示頁碼；十位數以上表示使用次數-1
        // 27代表7這頁使用過3次
        for (int i = 0; i < references.length; i++) {
            int page = references[i]; // 要 reference 的 page
            
            // 在 page frames 中尋找此 page
            boolean found = false;
            for (int j = 0; j < pages.size(); j++ ) {
                if ((pages.get(j) % 10) == page) {
                    found = true;
                    pages.add(pages.remove(j) + 10); // 次數加一並更新順序
                    break;
                }
            }
            
            if (!found) {
                faultCount++;
                if (pages.size() < frameCount) { // 還有空的頁框
                    pages.add(page);
                } else { // 頁置換
                    replaceCount++;
                    // 先找到最小的次數
                    int least = pages.get(0) / 10;
                    for (int j = 1; j < pages.size(); j++) {
                        if (pages.get(j) / 10 < least) {
                            least = pages.get(j) / 10;
                        }
                    }
                    // 再置換次數最小者
                    for (int j = 0; j < pages.size(); j++) {
                        if (pages.get(j) / 10 == least) {
                            pages.remove(j);
                            pages.add(page);
                            break;
                        }
                    }
                }
            }
            
            write(page + "\t");
            for (int j = pages.size()-1; j >= 0; j--) {
                write(pages.get(j) % 10);
            }
            if (!found) {
                write("\tF");
            }
            writeln();
        }

        write("Page Fault = " + faultCount);
        write("  Page Replaces = " + replaceCount);
        writeln("  Page Frames = " + frameCount);
    }

    private void mfuLru() {
        ArrayList<Integer> pages = new ArrayList<Integer>(); // page frames
        int faultCount = 0;
        int replaceCount = 0;
        
        writeln("\n--------------Most Frequently Used LRU Page Replacement -----------------------");

        // 用個位數表示頁碼；十位數以上表示使用次數-1
        // 27代表7這頁使用過3次
        for (int i = 0; i < references.length; i++) {
            int page = references[i]; // 要 reference 的 page
            
            // 在 page frames 中尋找此 page
            boolean found = false;
            for (int j = 0; j < pages.size(); j++ ) {
                if ((pages.get(j) % 10) == page) {
                    found = true;
                    pages.add(pages.remove(j) + 10); // 次數加一並更新順序
                    break;
                }
            }
            
            if (!found) {
                faultCount++;
                if (pages.size() < frameCount) { // 還有空的頁框
                    pages.add(page);
                } else { // 頁置換
                    replaceCount++;
                    // 先找到最大的次數
                    int max = 0;
                    for (int j = 0; j < pages.size(); j++) {
                        if (pages.get(j) / 10 > max) {
                            max = pages.get(j) / 10;
                        }
                    }
                    // 再置換次數最大者
                    for (int j = 0; j < pages.size(); j++) {
                        if (pages.get(j) / 10 == max) {
                            pages.remove(j);
                            pages.add(page);
                            break;
                        }
                    }
                }
            }
            
            write(page + "\t");
            for (int j = pages.size()-1; j >= 0; j--) {
                write(pages.get(j) % 10);
            }
            if (!found) {
                write("\tF");
            }
            writeln();
        }

        write("Page Fault = " + faultCount);
        write("  Page Replaces = " + replaceCount);
        writeln("  Page Frames = " + frameCount);
    }
}
