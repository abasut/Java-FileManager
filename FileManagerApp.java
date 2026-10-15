import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileManagerApp extends JFrame {

    private JTree tree;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;
    private File currentDir;
    private List<File> currentFiles = new ArrayList<>();
    private File rootDir;

    public FileManagerApp() {
        rootDir = new File(System.getProperty("user.home"));
        currentDir = rootDir;

        setTitle("Файловый менеджер");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createMenu();
        createTree();
        createTable();
        createStatusBar();

        updateTable(currentDir);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem refreshItem = new JMenuItem("Обновить");
        refreshItem.addActionListener(e -> {
            if (currentDir != null) {
                updateTable(currentDir);
            } else {
                JOptionPane.showMessageDialog(this, "Папка не выбрана", "Внимание", JOptionPane.WARNING_MESSAGE);
            }
        });
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("Вид");
        JMenuItem sortName = new JMenuItem("Сортировка по имени");
        sortName.addActionListener(e -> sortAndUpdate(Comparator.comparing(f -> f.getName().toLowerCase())));
        JMenuItem sortSize = new JMenuItem("Сортировка по размеру");
        sortSize.addActionListener(e -> sortAndUpdate(Comparator.comparingLong(File::length).reversed()));
        JMenuItem sortDate = new JMenuItem("Сортировка по дате");
        sortDate.addActionListener(e -> sortAndUpdate(Comparator.comparingLong(File::lastModified).reversed()));
        viewMenu.add(sortName);
        viewMenu.add(sortSize);
        viewMenu.add(sortDate);

        JMenu helpMenu = new JMenu("Помощь");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Файловый менеджер\nАвтор: Студент\nДата: 15.10.2026",
                "О программе", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void sortAndUpdate(Comparator<File> comparator) {
        if (currentDir == null) return;
        currentFiles.sort((a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return comparator.compare(a, b);
        });
        fillTableFromList();
    }

    private void createTree() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootDir);
        List<File> children = FileSystemHelper.listFiles(rootDir);
        for (File f : children) {
            if (f.isDirectory()) {
                rootNode.add(new DefaultMutableTreeNode(f));
            }
        }

        tree = new JTree(new DefaultTreeModel(rootNode));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) return;
                Object obj = node.getUserObject();
                if (!(obj instanceof File)) return;
                File f = (File) obj;
                if (f.isDirectory()) {
                    currentDir = f;
                    updateTable(currentDir);
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(250, 0));
        add(treeScroll, BorderLayout.WEST);
    }

    private void createTable() {
        String[] columns = {"Имя", "Размер", "Тип", "Изменён"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row < 0 || row >= currentFiles.size()) return;
                    File f = currentFiles.get(row);
                    if (f.isDirectory()) {
                        currentDir = f;
                        updateTable(currentDir);
                    } else {
                        openFile(f);
                    }
                }
            }
        });

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int row = table.getSelectedRow();
                if (row < 0 || row >= currentFiles.size()) {
                    statusLabel.setText("Выбрано: —");
                    return;
                }
                File f = currentFiles.get(row);
                String size = f.isDirectory() ? "" : FileSystemHelper.formatSize(f.length());
                String date = FileSystemHelper.formatDate(f.lastModified());
                statusLabel.setText("Выбрано: " + f.getName() + "  |  " + size + "  |  " + date);
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        add(tableScroll, BorderLayout.CENTER);
    }

    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusLabel = new JLabel("Выбрано: —");
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Обновить");
        refreshBtn.addActionListener(e -> {
            if (currentDir != null) {
                updateTable(currentDir);
            } else {
                JOptionPane.showMessageDialog(this, "Папка не выбрана", "Внимание", JOptionPane.WARNING_MESSAGE);
            }
        });
        statusPanel.add(refreshBtn, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);
    }

    private void updateTable(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        currentFiles = FileSystemHelper.listFiles(dir);
        currentFiles.sort((a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        fillTableFromList();
        statusLabel.setText("Выбрано: —");
    }

    private void fillTableFromList() {
        tableModel.setRowCount(0);
        for (File f : currentFiles) {
            String name = f.getName();
            String size = f.isDirectory() ? "" : FileSystemHelper.formatSize(f.length());
            String type = f.isDirectory() ? "Папка" : "Файл";
            String date = FileSystemHelper.formatDate(f.lastModified());
            tableModel.addRow(new Object[]{name, size, type, date});
        }
    }

    private void openFile(File file) {
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "Открытие файлов не поддерживается", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Не удалось открыть файл:\n" + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileManagerApp app = new FileManagerApp();
            app.setVisible(true);
        });
    }
}
