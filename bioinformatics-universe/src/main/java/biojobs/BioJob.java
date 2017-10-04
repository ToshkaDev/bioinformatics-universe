package biojobs;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name="biojobs")
public class BioJob {

    @Id
    @SequenceGenerator(name="pk_sequence", sequenceName="biojobs_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="pk_sequence")
    @Column(name="ID")
    private int id;

    @NotNull
    @Column(name="JOBID")
    private int jobId;

    @NotNull
    @Column(name="FINISHED")
    private boolean finished;

    @NotNull
    @Column(name="PROGRAM_NAME")
    private String programName;

    @NotNull
    @Column(name="RESULT_FILE_NAME")
    private String resultFileName;

    @NotNull
    @Column(name="RESULT_FILE")
    private String resultFile;

    @Column(name="EMAIL")
    private String email;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "JOB_DATE", columnDefinition= "TIMESTAMP WITHOUT TIME ZONE")
    private LocalDateTime jobDate;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getProgramNameName() {
        return programName;
    }

    public void setProgramNameName(String jobName) {
        this.programName = programName;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getJobDate() {
        return jobDate;
    }

    public void setJobDate(LocalDateTime jobDate) {
        this.jobDate = jobDate;
    }

}
