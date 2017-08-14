package serviceimpl;

import static com.google.common.base.Strings.isNullOrEmpty;
import static converters.ConverterMain.fromSeqRequestToSeqInternal;
import static org.apache.naming.SelectorContext.prefix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import exceptions.IncorrectRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.internal.SequenceInternal;
import model.request.SequenceRequest;
import service.SequenceService;
import service.StorageService;

@Service
public class SequenceServiceImpl extends BioUniverseServiceImpl implements SequenceService {
	private final String getSeqByName;
	private final String makeUnique;



	public SequenceServiceImpl(StorageService storageService, AppProperties properties) {
		super(storageService, properties);
		this.getSeqByName = super.getProperties().getGetSeqByNameProgram();
		this.makeUnique = super.getProperties().getMakeUniqueProgram();
	}

	@Override
	public String getByName(SequenceRequest sequenceRequest) throws IncorrectRequestException {
		return launchProcessAndGetResultFileName(sequenceRequest, getSeqByName);
	}

	@Override
	public String makeUnique(SequenceRequest sequenceRequest) throws IncorrectRequestException {
		return launchProcessAndGetResultFileName(sequenceRequest, makeUnique);
	}

	@Override
	public String extract() {
		return "";
	}

	public String launchProcessAndGetResultFileName(SequenceRequest sequenceRequest, String commandName) throws IncorrectRequestException {
		SequenceInternal sequenceInternal = storeFileAndGetInternalRepresentation(sequenceRequest);
		sequenceInternal.setAllFields();

		String resultFileName = super.getPrefix() + UUID.randomUUID().toString() + ".txt";
		File outputFile = new File(super.getWorkingDir() + resultFileName);


		List<String> commandArguments = sequenceInternal.getAllFields();
		commandArguments.add(0, super.getPython());
		commandArguments.add(1, commandName);

		ProcessBuilder processBuilder = new ProcessBuilder(commandArguments);
		processBuilder.directory(new File(super.getWorkingDir()));
		try {
			Process process = processBuilder.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			while ((line = br.readLine()) != null) {
				bw.write(line);
				bw.write("\n");
			}
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputFile.getName();
	}

	public SequenceInternal storeFileAndGetInternalRepresentation(SequenceRequest sequenceRequest) throws IncorrectRequestException {
		String firstFileName = null;
		String secondFileName = null;

		if (sequenceRequest.getFirstFile() != null) {
			if (!isNullOrEmpty(sequenceRequest.getFirstFileTextArea())) {
				throw new IncorrectRequestException("firstFileTextArea and firstFileName are both not empty");
			} else {
				firstFileName = super.getStorageService().store(sequenceRequest.getFirstFile());
			}
		} else if (!isNullOrEmpty(sequenceRequest.getFirstFileTextArea())) {
			firstFileName = super.getStorageService().createAndStore(sequenceRequest.getFirstFileTextArea());
		}

		if (sequenceRequest.getSecondFile() != null) {
			if (!isNullOrEmpty(sequenceRequest.getSecondFileTextArea())) {
				throw new IncorrectRequestException("secondFileTextArea and firstFileName are both not empty");
			} else {
				secondFileName = super.getStorageService().store(sequenceRequest.getSecondFile());
			}
		} else if (!isNullOrEmpty(sequenceRequest.getSecondFileTextArea())) {
			secondFileName = super.getStorageService().createAndStore(sequenceRequest.getSecondFileTextArea());
		}
		SequenceInternal sequenceInternal = fromSeqRequestToSeqInternal(sequenceRequest, firstFileName, secondFileName);
		return sequenceInternal;
	}
}
