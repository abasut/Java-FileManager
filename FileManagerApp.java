import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

// Простой файловый менеджер.
// Слева - дерево папок, в центре - таблица с файлами, внизу - строка состояния.
public class FileManagerApp extends JFrame implements ActionListener, TreeSelectionListener {

    // компоненты окна
    JTree tree;
    JTable table;
    DefaultTableModel tableModel;
    JLabel statusLabel;
    JButton refreshButton;

    // пункты меню (сохраняю в поля, чтобы потом сравнивать в actionPerformed)
    JMenuItem refreshItem;
    JMenuItem exitItem;
    JMenuItem sortNameItem;
    JMenuItem sortSizeItem;
    JMenuItem sortDateItem;
    JMenuItem aboutItem;

    // данные
    File currentDir;                                      // папка, которую сейчас показываем
    ArrayList<File> currentFiles = new ArrayList<File>(); // её содержимое

    public FileManagerApp() {

        // начинаем с домашней папки пользователя
        currentDir = new File(System.getProperty("user.home"));

        setTitle("Файловый менеджер");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // окно по центру экрана
        setLayout(new BorderLayout());

        // ================= МЕНЮ =================
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        refreshItem = new JMenuItem("Обновить");
        refreshItem.addActionListener(this);
        exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(this);
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("Вид");
        sortNameItem = new JMenuItem("Сортировка по имени");
        sortNameItem.addActionListener(this);
        sortSizeItem = new JMenuItem("Сортировка по размеру");
        sortSizeItem.addActionListener(this);
        sortDateItem = new JMenuItem("Сортировка по дате");
        sortDateItem.addActionListener(this);
        viewMenu.add(sortNameItem);
        viewMenu.add(sortSizeItem);
        viewMenu.add(sortDateItem);

        JMenu helpMenu = new JMenu("Помощь");
        aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(this);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // ================= ДЕРЕВО ПАПОК (слева) =================
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(currentDir);

        // добавляем в дерево только папки первого уровня
        ArrayList<File> list = FileSystemHelper.getFiles(currentDir);
        for (int i = 0; i < list.size(); i++) {
            File f = list.get(i);
            if (f.isDirectory()) {
                root.add(new DefaultMutableTreeNode(f));
            }
        }

        tree = new JTree(root);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(250, 0));
        add(treeScroll, BorderLayout.WEST);

        // ================= ТАБЛИЦА (в центре) =================
        String[] columns = {"Имя", "Размер", "Тип", "Изменён"};
        tableModel = new DefaultTableModel(columns, 0) {
            // чтобы нельзя было редактировать ячейки мышкой
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // двойной клик - зайти в папку или открыть файл
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });

        // выделение строки - показываем инфу внизу
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                showInfo();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        add(tableScroll, BorderLayout.CENTER);

        // ================= СТРОКА СОСТОЯНИЯ (внизу) =================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        statusLabel = new JLabel("Выбрано: нет");
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(this);
        bottomPanel.add(refreshButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // сразу показываем домашнюю папку
        showDirectory(currentDir);
    }

    // Сюда приходят нажатия на все кнопки и пункты меню
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == refreshItem || source == refreshButton) {
            if (currentDir == null) {
                JOptionPane.showMessageDialog(this, "Папка не выбрана",
                        "Внимание", JOptionPane.WARNING_MESSAGE);
            } else {
                showDirectory(currentDir);
            }
        } else if (source == exitItem) {
            System.exit(0);
        } else if (source == sortNameItem) {
            sortFiles("name");
            fillTable();
        } else if (source == sortSizeItem) {
            sortFiles("size");
            fillTable();
        } else if (source == sortDateItem) {
            sortFiles("date");
            fillTable();
        } else if (source == aboutItem) {
            JOptionPane.showMessageDialog(this,
                    "Файловый менеджер\nАвтор: Студент\nДата: 15.10.2026",
                    "О программе", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Клик по папке в дереве
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        Object obj = node.getUserObject();
        if (obj instanceof File) {
            File f = (File) obj;
            if (f.isDirectory()) {
                currentDir = f;
                showDirectory(currentDir);
            }
        }
    }

    // Читаем папку и показываем её в таблице
    void showDirectory(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }

        currentFiles = FileSystemHelper.getFiles(dir);
        sortFiles("name"); // по умолчанию сортируем по имени
        fillTable();

        statusLabel.setText("Выбрано: нет");
        setTitle("Файловый менеджер - " + dir.getPath());
    }

    // Заполняем таблицу из списка currentFiles
    void fillTable() {
        tableModel.setRowCount(0); // сначала чистим таблицу

        for (int i = 0; i < currentFiles.size(); i++) {
            File f = currentFiles.get(i);

            String name = f.getName();

            String size = "";
            if (!f.isDirectory()) {
                size = FileSystemHelper.formatSize(f.length());
            }

            String type = FileSystemHelper.getType(f);
            String date = FileSystemHelper.formatDate(f.lastModified());

            String[] row = {name, size, type, date};
            tableModel.addRow(row);
        }
    }

    // Сортировка списка файлов "пузырьком".
    // type: "name" - по имени, "size" - по размеру, "date" - по дате.
    // Папки всегда идут выше файлов.
    void sortFiles(String type) {
        for (int i = 0; i < currentFiles.size() - 1; i++) {
            for (int j = 0; j < currentFiles.size() - 1 - i; j++) {

                File a = currentFiles.get(j);
                File b = currentFiles.get(j + 1);

                boolean nadoPomenyat = false;

                if (!a.isDirectory() && b.isDirectory()) {
                    // папка должна быть выше файла
                    nadoPomenyat = true;
                } else if (a.isDirectory() == b.isDirectory()) {

                    if (type.equals("name")) {
                        if (a.getName().compareToIgnoreCase(b.getName()) > 0) {
                            nadoPomenyat = true;
                        }
                    } else if (type.equals("size")) {
                        if (a.length() < b.length()) { // сначала большие
                            nadoPomenyat = true;
                        }
                    } else if (type.equals("date")) {
                        if (a.lastModified() < b.lastModified()) { // сначала новые
                            nadoPomenyat = true;
                        }
                    }
                }

                if (nadoPomenyat) {
                    currentFiles.set(j, b);
                    currentFiles.set(j + 1, a);
                }
            }
        }
    }

    // Информация о выделенном файле в строке состояния
    void showInfo() {
        int row = table.getSelectedRow();

        if (row < 0 || row >= currentFiles.size()) {
            statusLabel.setText("Выбрано: нет");
            return;
        }

        File f = currentFiles.get(row);

        String size = "";
        if (!f.isDirectory()) {
            size = FileSystemHelper.formatSize(f.length());
        }
        String date = FileSystemHelper.formatDate(f.lastModified());

        statusLabel.setText("Выбрано: " + f.getName() + "   " + size + "   " + date);
    }

    // Двойной клик по строке таблицы
    void openSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentFiles.size()) {
            return;
        }

        File f = currentFiles.get(row);

        if (f.isDirectory()) {
            // заходим внутрь папки
            currentDir = f;
            showDirectory(currentDir);
        } else {
            // пробуем открыть файл стандартной программой
            try {
                Desktop.getDesktop().open(f);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Не получилось открыть файл:\n" + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        FileManagerApp window = new FileManagerApp();
        window.setVisible(true);
    }
}