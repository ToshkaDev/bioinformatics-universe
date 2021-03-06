package serviceimpl;

import static converters.ConverterMain.fromEvolRequestToEvolInternal;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import biojobs.BioJob;
import biojobs.BioJobDao;
import biojobs.BioJobResult;
import biojobs.BioJobResultDao;
import enums.ParamPrefixes;
import model.internal.EvolutionInternal;
import model.request.EvolutionRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import service.EvolutionService;
import service.StorageService;
import exceptions.IncorrectRequestException;
import springconfiguration.AppProperties;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class EvolutionServiceImpl extends BioUniverseServiceImpl implements EvolutionService {
	private final String prepareNames;
	private final String blastAllVsAll;
	private final String alignMultiple;

	private final int defaultLastJobId = 1;


	public EvolutionServiceImpl(final StorageService storageService, final AppProperties properties, final BioJobDao bioJobDao, final BioJobResultDao bioJobResultDao) {
		super(storageService, properties, bioJobResultDao, bioJobDao);
		this.prepareNames = properties.getPrepareNamesProgram();
		this.blastAllVsAll = properties.getBlastAllVsAllProgram();
		this.alignMultiple = properties.getAlignMultiple();
	}

	@Override
	public BioJob getBioJobIfFinished(int jobId) {
		BioJob bioJob = super.getBioJobDao().findByJobId(jobId);
		return bioJob.isFinished() ? bioJob : null;
	}

    @Override
	public String[] createDirsConcat() {
		//i-input, o-output
		String iFilesLocationAlign= super.getProperties().getMultipleWorkingFilesLocation();
		String oFilesLocationAlign = super.getProperties().getMultipleWorkingFilesLocation();
		String iFilesLocationConcatenate = oFilesLocationAlign;
		super.getStorageService().createMultipleDirs(Arrays.asList(iFilesLocationAlign, oFilesLocationAlign));
		return new String[] {iFilesLocationAlign, oFilesLocationAlign, iFilesLocationConcatenate};
	}


    @Override
	public String[] createDirs() {
	    //i-input, o-output
        String iFilesLocationPrepNames = super.getProperties().getMultipleWorkingFilesLocation();
        String oFilesLocationPrepNames = super.getProperties().getMultipleWorkingFilesLocation();
        String iFilesLocationBlast = oFilesLocationPrepNames;
        String oFilesLocationBlast = super.getProperties().getMultipleWorkingFilesLocation();
        String iFilesLocationCreateCogs = oFilesLocationBlast;
        super.getStorageService().createMultipleDirs(Arrays.asList(iFilesLocationPrepNames, oFilesLocationPrepNames, oFilesLocationBlast));
        return new String[] {iFilesLocationPrepNames, oFilesLocationPrepNames, iFilesLocationBlast, oFilesLocationBlast, iFilesLocationCreateCogs};
    }

    @Override
    public EvolutionInternal storeFilesAndPrepareCommandArgumentsConcat(final EvolutionRequest evolutionRequest, String[] locations) throws IncorrectRequestException {
        EvolutionInternal evolutionInternal = storeFileAndGetInternalRepresentation(evolutionRequest, locations[0]);
        evolutionInternal.setFields();
        evolutionInternal.setOutputFileName(super.getPrefix() + UUID.randomUUID().toString() + super.getPostfix());


        List<String> argsForAlignMultiple = new LinkedList<>();
        List<String> argsForConcatenate = new LinkedList<>();

        argsForAlignMultiple.addAll(Arrays.asList(ParamPrefixes.INPUT.getPrefix()+locations[0], ParamPrefixes.OUTPUT.getPrefix()+locations[1]));

        argsForConcatenate.add(ParamPrefixes.INPUT.getPrefix()+locations[2]);
        argsForConcatenate.add(ParamPrefixes.OUTPUT.getPrefix() + evolutionInternal.getOutputFileName());
        argsForConcatenate.addAll(evolutionInternal.getAllFields());

        String[] arrayOfInterpreters = {super.getBash(), super.getPython()};
        String[] arrayOfPrograms = {alignMultiple, super.getProgram(evolutionInternal.getCommandToBeProcessedBy())};
        List<List<String>> listOfArgumentLists = new LinkedList<>(Arrays.asList(argsForAlignMultiple, argsForConcatenate));
        prepareCommandArgumentsCommon(evolutionInternal, arrayOfInterpreters, arrayOfPrograms, listOfArgumentLists);

        return evolutionInternal;
    }

//    public EvolutionInternal storeFilesAndPrepareCommandArgumentsU(final EvolutionRequest evolutionRequest,
//                                                                   String[] locations,
//                                                                   int numberOfIntermScripts,
//                                                                   Map<Integer, List<String>> intermediateParams) throws IncorrectRequestException {
//        EvolutionInternal evolutionInternal = storeFileAndGetInternalRepresentation(evolutionRequest, locations[0]);
//        evolutionInternal.setFields();
//        evolutionInternal.setOutputFileName(super.getPrefix() + UUID.randomUUID().toString() + super.getPostfix());
//
//        List<List<String>> listOfArgumentLists = new LinkedList<>();
//        List<String> argsForScript = new LinkedList<>();
//
//        for (int i = 0; i < numberOfIntermScripts; i++) {
//            argsForScript = new LinkedList<>();
//            argsForScript.addAll(Arrays.asList(ParamPrefixes.INPUT.getPrefix()+locations[i], ParamPrefixes.OUTPUT.getPrefix()+locations[i+1]));
//            if (intermediateParams.containsKey(i)) {
//                argsForScript.addAll(intermediateParams.get(i));
//            }
//            listOfArgumentLists.add(argsForScript);
//        }
//
//        argsForScript.add(ParamPrefixes.INPUT.getPrefix()+locations[locations.length-1]);
//        argsForScript.addAll(evolutionInternal.getAllFields());
//
//
//	    return null;
//    }

    @Override
    public EvolutionInternal storeFilesAndPrepareCommandArguments (final EvolutionRequest evolutionRequest, String[] locations) throws IncorrectRequestException {
	    EvolutionInternal evolutionInternal = storeFileAndGetInternalRepresentation(evolutionRequest, locations[0]);
        evolutionInternal.setFields();
        evolutionInternal.setOutputFileName(super.getPrefix() + UUID.randomUUID().toString() + super.getPostfix());

        List<String> argsForPrepNames = new LinkedList<>();
        List<String> argsForBlast = new LinkedList<>();
        List<String> argsForCreateCogs = new LinkedList<>();
        argsForPrepNames.addAll(Arrays.asList(ParamPrefixes.INPUT.getPrefix()+locations[0], ParamPrefixes.OUTPUT.getPrefix()+locations[1]));

        argsForBlast.add(ParamPrefixes.WDIR.getPrefix() + super.getPathToMainDirFromBioProgs() + super.getWorkingDir()+"/");
        argsForBlast.addAll(Arrays.asList(ParamPrefixes.INPUT.getPrefix()+locations[2], ParamPrefixes.OUTPUT.getPrefix()+locations[3]));

        argsForCreateCogs.add(ParamPrefixes.INPUT.getPrefix()+locations[4]);
        argsForCreateCogs.add(ParamPrefixes.OUTPUT.getPrefix() + evolutionInternal.getOutputFileName());
        argsForCreateCogs.addAll(evolutionInternal.getAllFields());


        String[] arrayOfInterpreters = {super.getBash(), super.getBash(), super.getPython()};
        String[] arrayOfPrograms = {prepareNames, blastAllVsAll, super.getProgram(evolutionInternal.getCommandToBeProcessedBy())};
        List<List<String>> listOfArgumentLists = new LinkedList<>(Arrays.asList(argsForPrepNames, argsForBlast, argsForCreateCogs));

        prepareCommandArgumentsCommon(evolutionInternal, arrayOfInterpreters, arrayOfPrograms, listOfArgumentLists);

        return evolutionInternal;
    }

    public void prepareCommandArgumentsCommon(EvolutionInternal evolutionInternal, String[] arrayOfInterpreters,
                                              String[] arrayOfPrograms, List<List<String>> listOfArgumentLists) {
        List<List<String>> commandsAndArguments = new LinkedList<>();

        for (int i=0; i< arrayOfPrograms.length; i++) {
            List<String> listOfCommandsAndArgs= new LinkedList<>();
            listOfCommandsAndArgs.add(arrayOfInterpreters[i]);
            listOfCommandsAndArgs.add(arrayOfPrograms[i]);
            listOfCommandsAndArgs.addAll(listOfArgumentLists.get(i));
            commandsAndArguments.add(listOfCommandsAndArgs);
        }
        int jobId = saveBioJobToDB(evolutionInternal);
        evolutionInternal.setJobId(jobId);
        evolutionInternal.setCommandsAndArguments(commandsAndArguments);
    }


    @Override
    @Async
    public void runMainProgram(EvolutionInternal evolutionInternal) throws IncorrectRequestException {
        for (List<String> commandArgument : evolutionInternal.getCommandsAndArguments()) {
            super.launchProcess(commandArgument);
        }
        saveResultFileToDB(evolutionInternal);
    }


	public int saveBioJobToDB(EvolutionInternal evolutionInternal) {
		int jobId = getLastJobId();

		BioJob bioJob = new BioJob();
		bioJob.setProgramNameName(super.getProgram(evolutionInternal.getCommandToBeProcessedBy()));
		bioJob.setJobId(jobId);
		bioJob.setJobDate(LocalDateTime.now());
		bioJob.setFinished(false);

		BioJobResult bioJobResult = new BioJobResult();
		bioJobResult.setResultFile("placeholder");
		bioJobResult.setResultFileName(evolutionInternal.getOutputFileName());
        bioJobResult.setBiojob(bioJob);

        bioJob.addToBioJobResultList(bioJobResult);
        super.getBioJobDao().save(bioJob);
		return jobId;
	}

	public void saveResultFileToDB(EvolutionInternal evolutionInternal) {
		File file = null;
		try {
			file = getStorageService().loadAsResource(evolutionInternal.getOutputFileName()).getFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder fileAsStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				fileAsStringBuilder.append(line + "\n");
			}
		} catch (FileNotFoundException e) {
			System.out.println("Can't find file " + file.toString());
		} catch (IOException e) {
			System.out.println("Unable to read file " + file.toString());
		}

		BioJobResult bioJobResult = super.getBioJobResultDao().findByResultFileName(evolutionInternal.getOutputFileName());
		bioJobResult.setResultFile(fileAsStringBuilder.toString());
        super.getBioJobResultDao().save(bioJobResult);

		BioJob bioJob = super.getBioJobDao().findByJobId(evolutionInternal.getJobId());
		bioJob.setFinished(true);
        super.getBioJobDao().save(bioJob);
	}

	private Integer getLastJobId() {
        Integer lastJobId = super.getBioJobDao().getLastJobId();
        return lastJobId != null ? lastJobId + 1 : defaultLastJobId;
	}

	private EvolutionInternal storeFileAndGetInternalRepresentation(final EvolutionRequest evolutionRequest, String inputFilesLocation1) throws IncorrectRequestException {
		super.getStorageService().storeMultipleFiles(evolutionRequest.getListOfFiles(), inputFilesLocation1);
		return fromEvolRequestToEvolInternal(evolutionRequest);
	}
}
