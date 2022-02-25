import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class JaKichuDiyeDe {
    static String target_file_name = "f_find_great_mentors.in.txt";

    static int C, P;
    static Contributer contributers[];
    static Project projects[];
    
    public static void main(String[] args) throws IOException {
        parse_input();  

        FileWriter out = new FileWriter("output_data/" + target_file_name);
        out.write(solve());
        out.close();
    }

    static void parse_input() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("input_data/" + target_file_name));
        
        String[] line1 = br.readLine().split(" ");
        C = Integer.parseInt(line1[0]);
        P = Integer.parseInt(line1[1]);

        Map<String, Integer> skill_code_dict = new HashMap<String, Integer>();
        contributers = new Contributer[C];
        projects = new Project[P];
        
        int skill_count = 0;

        for (int i = 0; i < C; i++) {
            String[] line2 = br.readLine().split(" ");
            String name = line2[0];
            int N_i = Integer.parseInt(line2[1]);

            contributers[i] = new Contributer(name, N_i);
            
            for (int j = 0; j < N_i; j++) {
                String[] line3 = br.readLine().split(" ");
                String skill_name = line3[0];
                int skill_level = Integer.parseInt(line3[1]);
                int skill_code = 0;
                
                if (skill_code_dict.containsKey(skill_name)) { skill_code = skill_code_dict.get(skill_name); }
                else { skill_code_dict.put(skill_name, skill_count); skill_code = skill_count++; }

                contributers[i].add_skill(skill_code, skill_level);
            }
        }

        for (int i = 0; i < P; i++) {
            String[] line4 = br.readLine().split(" ");
            String name = line4[0];
            int D_i = Integer.parseInt(line4[1]);
            int S_i = Integer.parseInt(line4[2]);
            int B_i = Integer.parseInt(line4[3]);
            int R_i = Integer.parseInt(line4[4]);

            projects[i] = new Project(name, D_i, S_i, B_i, R_i);

            for (int j = 0; j < R_i; j++) {
                String[] line5 = br.readLine().split(" ");
                int skill_code = skill_code_dict.get(line5[0]);
                int skill_level = Integer.parseInt(line5[1]);

                projects[i].add_skill_requirement(skill_code, skill_level);
            }
        }

        br.close();
    }

    static class Contributer {
        private String name;
        private int N;
        private Map<Integer, Integer> skills;
        private boolean isWorking;
        
        Contributer(String name, int N) {
            skills = new HashMap<Integer, Integer>();
            
            this.name = name;
            this.N = N;
            this.isWorking = false;
        }
        
        void toggle_work() {
            this.isWorking = !this.isWorking;
        }

        boolean isWorking() {
            return this.isWorking;
        }

        boolean can_work(Skill skill) {
            return skill.skill_level <= get_skill_value(skill.skill_code);
        }

        boolean can_level_up(int skill_code, int skill_level) {
            return get_skill_value(skill_code) - skill_level <= 0;
        }

        void level_up(int skill_code) {
            if (skills.containsKey(skill_code)) { skills.put(skill_code, skills.get(skill_code) + 1); }
            else { skills.put(skill_code, 1); N++; }
        }

        void add_skill(int skill_code, int skill_level) {
            skills.put(skill_code, skill_level);
        }
        
        int get_skill_value(int skill_code) {
            return skills.containsKey(skill_code) ? skills.get(skill_code) : 0;
        }

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name; 
        }

        int get_skill_count() {
            return this.N;
        }

        Contributer union(Contributer other) {
            Contributer union = new Contributer("HIVEMIND", -1);
            
            Iterator<Integer> my_skills = this.skills.keySet().iterator();
            Iterator<Integer> their_skills = other.skills.keySet().iterator();
            
            while (my_skills.hasNext()) {
                int skill_code = my_skills.next();
                union.add_skill(skill_code, Math.max(this.get_skill_value(skill_code), other.get_skill_value(skill_code)));
            }

            while (their_skills.hasNext()) {
                int skill_code = their_skills.next();
                union.add_skill(skill_code, Math.max(this.get_skill_value(skill_code), other.get_skill_value(skill_code)));
            }

            return union;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    static class Skill {
        public int skill_code, skill_level;
        public int original_index;

        Skill(int skill_code, int skill_level, int original_index) {
            this.skill_code = skill_code;
            this.skill_level = skill_level;
            this.original_index = original_index;
        }
    }
    
    static class Project implements Comparable<Project> {
        private String name;
        private int D, S, B, R;
        private LinkedList<Skill> roles;

        int value;

        Project(String name, int D, int S, int B, int R) {
            roles = new LinkedList<Skill>();
            
            this.name = name;
            this.D = D;
            this.S = S;
            this.B = B;
            this.R = R; 
            this.value = 0;
        }

        @Override
        public int compareTo(Project other) {
            return other.value - this.value;
        }

        void update_value(int start_day) {
            value = 10*score(start_day) - 4*D - 3*R;
        }

        Iterator<Skill> get_skill_list() {
            return roles.iterator();
        }
        
        void add_skill_requirement(int skill_code, int skill_level) {
            roles.add(new Skill(skill_code, skill_level, roles.size()));
        }

        int score(int start_day) {
            if (start_day + D < B) { return S; }
            else { return Math.max(S - (start_day + D - B), 0); }
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    static class ProjectExecution implements Comparable<ProjectExecution> {
        Contributer[] contributers;

        int project_finish_date;
        
        final Project project;

        ProjectExecution(Project project) {
            this.contributers = new Contributer[project.R];
            this.project_finish_date = 0;

            this.project = project;
        }

        @Override
        public int compareTo(ProjectExecution other) {
            return this.project_finish_date - other.project_finish_date;
        }

        void start_execution(int start_day) {
            this.project_finish_date = start_day + project.D;

            for (Contributer contributer : contributers) {
                contributer.toggle_work();
            }
        }

        void complete_execution() {
            if (project_finish_date == 0) { return; }

            Iterator<Skill> roles_list = project.get_skill_list();
            for (Contributer contributer : contributers) {
                contributer.toggle_work();

                Skill cnt_skill = roles_list.next();
                int skill_code = cnt_skill.skill_code;
                int skill_level = cnt_skill.skill_level;
                
                if (contributer.can_level_up(skill_code, skill_level)) { contributer.level_up(skill_code); }
            }
        }

        @Override
        public String toString() {
            String event_log = "";
            
            for (Contributer contributer : contributers) {
                event_log += " " + contributer;
            }
            event_log = event_log.substring(1);
            event_log = project + "\n" + event_log + "\n";

            return event_log;
        }

        boolean executable() {
            Collections.sort(project.roles, (skill1, skill2) -> skill2.skill_level - skill1.skill_level);

            Contributer union = new Contributer("HIVEMIND", -1);

            boolean executable = true;

            boolean[] has_been_assigned = new boolean[C];
            for (Skill cnt_skill : project.roles) {
                int best_skill_value = Integer.MAX_VALUE;
                int best_choice = -1;
                
                boolean can_be_mentored = union.can_work(cnt_skill);

                for (int i = 0; i < C; i++) {
                    if (!JaKichuDiyeDe.contributers[i].isWorking() && !has_been_assigned[i]) {
                        int cnt_skill_value = JaKichuDiyeDe.contributers[i].get_skill_value(cnt_skill.skill_code);
                        if (cnt_skill_value >= best_skill_value) { continue; }
                        if (cnt_skill_value >= cnt_skill.skill_level || ( (cnt_skill_value == cnt_skill.skill_level - 1) && can_be_mentored)) { 
                            best_skill_value = cnt_skill_value; best_choice = i; 
                            if (best_skill_value == (cnt_skill.skill_level + (can_be_mentored ? -1 : 0))) { break; }
                        }
                    }
                }

                if (best_choice == -1) { 
                    executable = false;
                    break;
                } else {
                    contributers[cnt_skill.original_index] = JaKichuDiyeDe.contributers[best_choice]; 
                    union = union.union(JaKichuDiyeDe.contributers[best_choice]);
                    has_been_assigned[best_choice] = true;
                }
            }

            Collections.sort(project.roles, (skill1, skill2) -> skill1.original_index - skill2.original_index);
            
            return executable;
        }
    }

    static String solve() {
        int E = 0;
        String output = "";

        long target_time = System.currentTimeMillis() + 7 * 60 * 1000;
        System.err.println(target_time);
        
        LinkedList<Project> list_of_projects = new LinkedList<Project>(Arrays.asList(projects));
        PriorityQueue<ProjectExecution> events = new PriorityQueue<ProjectExecution>();
        events.add(new ProjectExecution(list_of_projects.get(0)));
        
        outer:
        while (events.size() > 0) {
            if ((E % 100 == 0) && System.currentTimeMillis() > target_time) { break outer; }
            
            ProjectExecution current_event = events.poll();
            current_event.complete_execution();
            int cnt_day = current_event.project_finish_date;
            
            for (Project project : list_of_projects) {
                project.update_value(cnt_day);
            }
    
            LinkedList<Project> undone_projects = new LinkedList<Project>();
            Collections.sort(list_of_projects);
    
            for (Project project : list_of_projects) {
                ProjectExecution attempt = new ProjectExecution(project);
                if (attempt.executable()) {
                    attempt.start_execution(cnt_day);
                    events.add(attempt);
                    E++;
                    output += attempt;
                } else {
                    undone_projects.add(project);
                }
            }
    
            list_of_projects = undone_projects;
        }

        output = E + "\n" + output;
        return output;
    }
}