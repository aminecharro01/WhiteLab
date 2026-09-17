package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.entities.enums.Jour;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.panels.Agenda.AgendaEditDialog;
import ma.WhiteLab.service.modules.agendas.api.AgendaMensuelService;
import ma.WhiteLab.service.modules.agendas.api.RendezVousService;
import ma.WhiteLab.service.modules.agendas.dto.AgendaMensuelDTO;
import ma.WhiteLab.service.modules.patient.api.PatientService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class AgendaMensuelPanel extends JPanel {

    private static final Color PRIMARY = new Color(0x0E, 0xA5, 0xA5);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color LIGHT_BG = new Color(245, 247, 250);
    private static final Color HOVER_BG = new Color(230, 230, 230);
    private static final Color NON_WORKING_BG = new Color(255, 210, 210);
    private static final Color RDV_BG = new Color(220, 235, 255);

    private final AgendaMensuelService agendaService;
    private final RendezVousService rdvService;
    private final Consumer<AgendaMensuelDTO> onChangeCallback;
    private final UserPrincipal principal;
    private final List<UserPrincipal> availableMedecins;

    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JComboBox<UserPrincipal> medecinCombo;
    private JPanel calendarGrid;
    private JLabel statusLabel;
    private JLabel monthYearLabel;

    private JPanel rdvPanel;
    private JLabel rdvDateLabel;
    private JTable rdvTable;
    private DefaultTableModel rdvTableModel;
    private JPanel emptyRdvPanel;
    private CardLayout rdvCardLayout;

    private YearMonth currentMonth = YearMonth.now();
    private Set<LocalDate> nonWorkingDays = new HashSet<>();
    private Map<LocalDate, List<RendezVous>> rdvByDay = new HashMap<>();
    private Long restrictedMedecinId;

    public AgendaMensuelPanel(
            AgendaMensuelService agendaService, RendezVousService rdvService, PatientService patientService,
            Long restrictedMedecinId, Consumer<AgendaMensuelDTO> onChangeCallback, UserPrincipal principal
    ) {
        this(agendaService, rdvService, patientService, restrictedMedecinId, onChangeCallback, principal, List.of());
    }

    public AgendaMensuelPanel(
            AgendaMensuelService agendaService, RendezVousService rdvService, PatientService patientService,
            Long restrictedMedecinId, Consumer<AgendaMensuelDTO> onChangeCallback, UserPrincipal principal,
            List<UserPrincipal> availableMedecins
    ) {
        this.agendaService = agendaService;
        this.rdvService = rdvService;
        this.onChangeCallback = onChangeCallback;
        this.principal = principal;
        this.availableMedecins = availableMedecins != null ? availableMedecins : List.of();
        this.restrictedMedecinId = restrictedMedecinId;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        setBackground(LIGHT_BG);

        initHeader();
        initMainContent();
        initBottomBar();

        // ───────────────────────────────────────────────────────────────
        // Critical fix: Initialize selectors and load data AFTER all components are created
        updateSelectors();
        loadData();
        // ───────────────────────────────────────────────────────────────
    }

    private void initHeader() {
        JPanel top = new JPanel(new BorderLayout(15, 10));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 230)));

        JLabel title = new JLabel("Agenda Mensuel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(PRIMARY);

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        monthYearLabel.setForeground(new Color(70, 90, 130));
        updateMonthLabel();

        JPanel nav = createNavigationPanel();

        top.add(title, BorderLayout.WEST);
        top.add(monthYearLabel, BorderLayout.CENTER);
        top.add(nav, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
    }

    private JPanel createNavigationPanel() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        nav.setBackground(Color.WHITE);

        JButton prev = new JButton("◄");
        styleNavButton(prev);
        prev.addActionListener(e -> navigateMonths(-1));

        JButton next = new JButton("►");
        styleNavButton(next);
        next.addActionListener(e -> navigateMonths(1));

        monthCombo = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            monthCombo.addItem(YearMonth.of(2000, i).getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRENCH));
        }
        monthCombo.addActionListener(e -> onDateSelectorChanged());

        yearCombo = new JComboBox<>();
        int currentYear = currentMonth.getYear();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            yearCombo.addItem(i);
        }
        yearCombo.addActionListener(e -> onDateSelectorChanged());

        // updateSelectors() was REMOVED from here — moved to constructor

        nav.add(prev);
        nav.add(next);
        nav.add(new JLabel("Mois:"));
        nav.add(monthCombo);
        nav.add(new JLabel("Année:"));
        nav.add(yearCombo);

        if (!availableMedecins.isEmpty()) {
            medecinCombo = new JComboBox<>(availableMedecins.toArray(new UserPrincipal[0]));
            medecinCombo.setSelectedItem(availableMedecins.stream()
                    .filter(m -> m.id().equals(restrictedMedecinId)).findFirst().orElse(null));
            medecinCombo.addActionListener(e -> {
                UserPrincipal selected = (UserPrincipal) medecinCombo.getSelectedItem();
                if (selected != null) {
                    this.restrictedMedecinId = selected.id();
                    loadData();
                }
            });
            nav.add(new JLabel("Médecin:"));
            nav.add(medecinCombo);
        }

        JButton refresh = new JButton("↻");
        styleNavButton(refresh);
        refresh.addActionListener(e -> loadData());
        nav.add(refresh);

        return nav;
    }

    private void initMainContent() {
        calendarGrid = new JPanel(new GridLayout(0, 7, 6, 6));
        calendarGrid.setBackground(Color.WHITE);
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane calendarScrollPane = new JScrollPane(calendarGrid);
        calendarScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        initRdvPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, calendarScrollPane, rdvPanel);
        splitPane.setDividerLocation(0.65);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    private void initRdvPanel() {
        rdvPanel = new JPanel(new BorderLayout(10, 10));
        rdvPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 15, 10, 15)
        ));
        rdvPanel.setBackground(Color.WHITE);

        rdvDateLabel = new JLabel("Aucun jour sélectionné", SwingConstants.CENTER);
        rdvDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rdvDateLabel.setForeground(PRIMARY);
        rdvPanel.add(rdvDateLabel, BorderLayout.NORTH);

        rdvCardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(rdvCardLayout);
        contentPanel.setOpaque(false);

        String[] columnNames = {"Heure", "Patient", "Motif", "Statut"};
        rdvTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rdvTable = new JTable(rdvTableModel);
        rdvTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rdvTable.setRowHeight(28);
        rdvTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(new JScrollPane(rdvTable), "TABLE");

        emptyRdvPanel = new JPanel(new GridBagLayout());
        emptyRdvPanel.setOpaque(false);
        JLabel emptyLabel = new JLabel("Aucun rendez-vous pour ce jour.", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emptyLabel.setForeground(Color.GRAY);
        emptyRdvPanel.add(emptyLabel);
        contentPanel.add(emptyRdvPanel, "EMPTY");

        rdvPanel.add(contentPanel, BorderLayout.CENTER);
        rdvCardLayout.show(contentPanel, "EMPTY");
    }

    private void initBottomBar() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(12, 12, 12, 12));
        bottom.setBackground(Color.WHITE);

        statusLabel = new JLabel("Prêt");
        statusLabel.setForeground(new Color(100, 100, 100));
        bottom.add(statusLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton createBtn = new JButton("Créer Agenda");
        styleActionButton(createBtn, SUCCESS);
        createBtn.addActionListener(e -> createMyAgenda());
        actions.add(createBtn);

        JButton editBtn = new JButton("Modifier Agenda");
        styleActionButton(editBtn, PRIMARY);
        editBtn.addActionListener(e -> editMyAgenda());
        actions.add(editBtn);

        bottom.add(actions, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void createMyAgenda() {
        Long medId = getSelectedMedecinId();
        if (medId == null) {
            JOptionPane.showMessageDialog(this, "Aucun médecin sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String moisFrancais = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);

        if (!agendaService.getAgendasMensuelsByMedecinIdAndMois(medId, moisFrancais).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Un agenda existe déjà pour ce mois.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        AgendaMensuelDTO newDto = new AgendaMensuelDTO();
        newDto.setMois(moisFrancais);
        newDto.setMedecinId(medId);
        newDto.setCreePar(principal.fullName());
        newDto.setJoursNonDisponible(new ArrayList<>());

        new AgendaEditDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                newDto,
                agendaService,
                createdDto -> {
                    onChangeCallback.accept(createdDto);
                    JOptionPane.showMessageDialog(this, "Agenda créé avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
        ).setVisible(true);
    }

    private void editMyAgenda() {
        Long medId = getSelectedMedecinId();
        String moisFrancais = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);

        List<AgendaMensuelDTO> agendas = agendaService.getAgendasMensuelsByMedecinIdAndMois(medId, moisFrancais);

        if (agendas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun agenda trouvé pour ce mois. Créez-en un.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        new AgendaEditDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                agendas.get(0),
                agendaService,
                updatedDto -> {
                    onChangeCallback.accept(updatedDto);
                    JOptionPane.showMessageDialog(this, "Agenda modifié avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
        ).setVisible(true);
    }

    private void displayRdvsForDate(LocalDate date, List<RendezVous> rdvs) {
        rdvDateLabel.setText("RDV du " + date.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)));
        rdvTableModel.setRowCount(0);

        if (rdvs == null || rdvs.isEmpty()) {
            rdvCardLayout.show((Container) rdvPanel.getComponent(1), "EMPTY");
        } else {
            rdvs.stream()
                    .sorted(Comparator.comparing(RendezVous::getDate))
                    .forEach(rdv -> {
                        String patientName = "N/A";
                        if (rdv.getDossierMed() != null && rdv.getDossierMed().getPat() != null) {
                            patientName = rdv.getDossierMed().getPat().getPrenom() + " " + rdv.getDossierMed().getPat().getNom();
                        }
                        rdvTableModel.addRow(new Object[]{
                                rdv.getDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                patientName,
                                rdv.getMotif(),
                                rdv.getStatus()
                        });
                    });
            rdvCardLayout.show((Container) rdvPanel.getComponent(1), "TABLE");
        }
    }

    public void loadData() {
        if (statusLabel != null) {
            statusLabel.setText("Chargement de " + currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)) + "...");
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Long medId = getSelectedMedecinId();
                if (medId == null) return null;

                String moisFrancais = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
                List<AgendaMensuelDTO> agendas = agendaService.getAgendasMensuelsByMedecinIdAndMois(medId, moisFrancais);
                AgendaMensuelDTO agenda = agendas.isEmpty() ? null : agendas.get(0);

                nonWorkingDays.clear();
                if (agenda != null && agenda.getJoursNonDisponible() != null) {
                    for (String jourStr : agenda.getJoursNonDisponible()) {
                        try {
                            DayOfWeek day = DayOfWeek.valueOf(jourStr.toUpperCase());
                            currentMonth.atDay(1).datesUntil(currentMonth.atEndOfMonth().plusDays(1))
                                    .filter(date -> date.getDayOfWeek() == day)
                                    .forEach(nonWorkingDays::add);
                        } catch (IllegalArgumentException e) {
                            // Skip invalid day names – log if needed
                        }
                    }
                }

                rdvByDay.clear();
                List<RendezVous> allRdv = rdvService.getRendezVousByMedecinAndPeriod(
                        medId,
                        currentMonth.atDay(1),
                        currentMonth.atEndOfMonth()
                );
                allRdv.forEach(rdv -> rdvByDay.computeIfAbsent(rdv.getDate().toLocalDate(), k -> new ArrayList<>()).add(rdv));
                return null;
            }

            @Override
            protected void done() {
                updateCalendarGrid();
                if (statusLabel != null) {
                    statusLabel.setText("Calendrier chargé. " + rdvByDay.values().stream().mapToInt(List::size).sum() + " RDV ce mois.");
                }
            }
        }.execute();
    }

    private void updateCalendarGrid() {
        calendarGrid.removeAll();
        String[] headers = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(PRIMARY);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            calendarGrid.add(lbl);
        }

        int offset = currentMonth.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < offset; i++) {
            calendarGrid.add(new JPanel());
        }

        for (int d = 1; d <= currentMonth.lengthOfMonth(); d++) {
            LocalDate date = currentMonth.atDay(d);
            calendarGrid.add(createDayCell(date, nonWorkingDays.contains(date), rdvByDay.getOrDefault(date, Collections.emptyList())));
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel createDayCell(LocalDate date, boolean isNonWorking, List<RendezVous> rdvs) {
        JPanel cell = new JPanel(new BorderLayout(5, 2));
        cell.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel num = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.LEFT);
        num.setBorder(new EmptyBorder(4, 8, 0, 0));
        num.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel indPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        indPanel.setOpaque(false);

        int rdvCount = rdvs.size();
        if (rdvCount > 0) {
            JLabel dot = new JLabel("●");
            dot.setForeground(PRIMARY.darker());
            dot.setFont(new Font("Segoe UI", Font.BOLD, 18));
            indPanel.add(dot);
            if (rdvCount > 1) {
                JLabel countLabel = new JLabel(String.valueOf(rdvCount));
                countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                countLabel.setForeground(Color.WHITE);
                countLabel.setBackground(PRIMARY);
                countLabel.setOpaque(true);
                countLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
                indPanel.add(countLabel);
            }
        }

        cell.add(num, BorderLayout.NORTH);
        cell.add(indPanel, BorderLayout.CENTER);

        if (isNonWorking) cell.setBackground(NON_WORKING_BG);
        else if (rdvCount > 0) cell.setBackground(RDV_BG);
        else cell.setBackground(Color.WHITE);

        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    displayRdvsForDate(date, rdvs);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                cell.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cell.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            }
        });

        return cell;
    }

    private void navigateMonths(int amount) {
        currentMonth = currentMonth.plusMonths(amount);
        updateSelectors();
        loadData();
    }

    private void onDateSelectorChanged() {
        int selectedYear = (Integer) yearCombo.getSelectedItem();
        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        YearMonth newMonth = YearMonth.of(selectedYear, selectedMonth);
        if (!newMonth.equals(currentMonth)) {
            currentMonth = newMonth;
            updateMonthLabel();
            loadData();
        }
    }

    private void updateSelectors() {
        monthCombo.setSelectedIndex(currentMonth.getMonthValue() - 1);
        yearCombo.setSelectedItem(currentMonth.getYear());
    }

    private void updateMonthLabel() {
        monthYearLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)));
    }

    private Long getSelectedMedecinId() {
        return restrictedMedecinId;
    }

    private void styleNavButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(HOVER_BG);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
    }

    private void styleActionButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 25, 10, 25));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    public void refreshData() {
        loadData();
    }
}