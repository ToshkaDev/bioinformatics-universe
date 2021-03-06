package controller;

import biojobs.BioJob;
import biojobs.BioJobResult;
import model.internal.EvolutionInternal;
import model.request.EvolutionRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import service.BioUniverseService;
import service.EvolutionService;
import service.StorageService;
import exceptions.IncorrectRequestException;
import enums.BioPrograms;



import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by vadim on 8/13/17.
 */

@Controller
@RequestMapping("/evolution")
public class EvolutionController extends BioUniverseController {

    @Autowired
    public final EvolutionService evolutionService;

    private static final Log logger = LogFactory.getLog(EvolutionController.class);

    private final List<String> statusReady = Arrays.asList("ready");
    private final List<String> statusNotReady = Arrays.asList("notReady");


	public EvolutionController(StorageService storageService, EvolutionService evolutionService) {
    	super(storageService);
    	this.evolutionService = evolutionService;
    }

    @GetMapping(value={"", "/", "/create-cogs"})
    public String createCogsPage(Model model) {

        addToModelCommon(model);
        model.addAttribute("subnavigationTab", BioPrograms.CREATE_COGS.getProgramName());
        return "main-view  :: addContent(" +
                "fragmentsMain='evolution-fragments', searchArea='evolution-create-cogs', " +
                "tab='evolution-navbar', filter='evolution-createCogs-filter')";
    }

    @GetMapping(value={"/concatenate"})
    public String concatenatePage(Model model) {
        addToModelCommon(model);
        model.addAttribute("subnavigationTab", BioPrograms.CONCATENATE.getProgramName());
        return "main-view  :: addContent(" +
                "fragmentsMain='evolution-fragments', searchArea='evolution-concatenate', " +
                "tab='evolution-navbar', filter='evolution-concatenate-filter')";
    }

    @PostMapping(value="/process-request", produces="text/plain")
    @ResponseBody
    public String processRequest(EvolutionRequest evolutionRequest) throws IncorrectRequestException, ExecutionException, InterruptedException {
        EvolutionInternal evolutionInternal = null;

        //Split it to several functions because 'createCogs' method is asynchronous
        //and files in 'listOfFiles' field of evolutionRequest are got cleared at the end of request processing.
        if (evolutionRequest.getCommandToBeProcessedBy().equals(BioPrograms.CREATE_COGS.getProgramName())) {
            String[] locations = evolutionService.createDirs();
            evolutionInternal = evolutionService.storeFilesAndPrepareCommandArguments(evolutionRequest, locations);
        } else if (evolutionRequest.getCommandToBeProcessedBy().equals(BioPrograms.CONCATENATE.getProgramName())) {
            String[] locations = evolutionService.createDirsConcat();
            evolutionInternal = evolutionService.storeFilesAndPrepareCommandArgumentsConcat(evolutionRequest, locations);
        }
        Integer jobId = evolutionInternal.getJobId();
        evolutionService.runMainProgram(evolutionInternal);

        return String.valueOf(jobId);
    }

    @GetMapping(value="/get-filename", produces="application/json")
    @ResponseBody
    public Map<String, List<String>> getFileNameIfReady(@RequestParam("jobId") Integer jobId) {
        BioJob bioJob;
        String urlPath = ServletUriComponentsBuilder.fromCurrentContextPath().path("/evolution/univ_files/").build().toString();

        Map<String, List<String>> result = new HashMap<>();
        result.put("status", statusNotReady);

        List<String> listOfResultFileNames = null;

	     if (jobId != null ) {
            bioJob = evolutionService.getBioJobIfFinished(jobId);
            if (bioJob != null)
                listOfResultFileNames = bioJob.getBioJobResultList().stream().map(bjResult -> urlPath + bjResult.getResultFileName()).collect(Collectors.toList());
                result.put("result", listOfResultFileNames);
                result.put("status", statusReady);
        }
        return result;
    }

    @GetMapping("/univ_files/{filename:.+}")
    public void getFileFromDb(@PathVariable String filename, HttpServletResponse response) throws IOException {
        BioJobResult bioJobResult = ((BioUniverseService) evolutionService).getBioJobResultDao().findByResultFileName(filename);

        response.setContentType("text/plain");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + bioJobResult.getResultFileName());

        OutputStream outputStream = response.getOutputStream();
        OutputStream buffOutputStream= new BufferedOutputStream(outputStream);
        OutputStreamWriter outputwriter = new OutputStreamWriter(buffOutputStream);

        outputwriter.write(bioJobResult.getResultFile());
        outputwriter.flush();
        outputwriter.close();
    }

    @Override
    void addToModelCommon(Model model) {
        model.addAttribute("mainTab", "evolution");
        model.addAttribute("specificJs", "/js/evolution.js");
    }

}
