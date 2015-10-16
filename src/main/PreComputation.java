package main;

import java.io.File;

import algorithms.ResidueCreator;
import algorithms.isomorphism.chains.ChainLearning;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;
import io.loaders.json.FamilyChainIO;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import model.Residue;

public class PreComputation {

	public static void main(String[] args) {
		//----------------- Parameters ---------------------------
		String rulesDBName = "data/rules.json";
		String monosDBName = "data/monomers.json";
		String jsonPolymers = "data/peptides.json";
		
		String jsonResidues = "data/residues.json";
		String jsonChains = "data/chains.json";
		
		// Parsing
		for (int idx=0 ; idx<args.length ; idx++) {
			if (args[idx].startsWith("-")) {
				switch (args[idx]) {
				case "-rul":
					rulesDBName = args[idx+1];
					break;
				case "-mono":
					monosDBName = args[idx+1];
					break;
				case "-poly":
					jsonPolymers = args[idx+1];
					break;
				case "-res":
					jsonResidues = args[idx+1];
					break;
				case "-cha":
					jsonChains = args[idx+1];
					break;

				default:
					System.err.println("Wrong option " + args[idx]);
					System.exit(1);
					break;
				}
				
				idx++;
			} else {
				System.err.println("Wrong parameter " + args[idx]);
				System.exit(1);
			}
		}
		
		// File existence
		File file = new File(rulesDBName);
		if (!file.exists()) {
			System.err.println("No file at " + rulesDBName);
			System.exit(1);
		}
		file = new File(monosDBName);
		if (!file.exists()) {
			System.err.println("No file at " + monosDBName);
			System.exit(1);
		}
		file = new File(jsonPolymers);
		if (!file.exists()) {
			System.err.println("No file at " + jsonPolymers);
			System.exit(1);
		}
		
		
		//----------------- residues --------------------------
        System.out.println("--- Loading ---");
        RulesDB rules = RulesJsonLoader.loader.loadFile(rulesDBName);
        MonomersDB monos = new MonomersJsonLoader().loadFile(monosDBName);
        PolymersJsonLoader pjl = new PolymersJsonLoader(monos);
        PolymersDB learningBase = pjl.loadFile(jsonPolymers);
        
        ResidueCreator rc = new ResidueCreator(rules);
        rc.setVerbose(false);

        System.out.println("--- Residues creation ---");
        FamilyDB families = rc.createResidues(monos);

        System.out.println("--- Saving residues ---");
        ResidueJsonLoader rjl = new ResidueJsonLoader(rules, monos);
        rjl.saveFile(families, jsonResidues);


        //----------------- chains ----------------------------
        System.out.println("--- Learning chains ---");
        // Adapt residue structures
        for (Residue res : families.getResidues().getObjects())
        	res.explicitToImplicitHydrogens();
        
        ChainLearning learning = new ChainLearning(learningBase);
        learning.setMarkovianSize(3);
        learning.learn(families);

        // --- Save ---
        System.out.println("--- Saving chains ---");
        FamilyChainIO fcio = new FamilyChainIO(families);
        fcio.saveFile(learning.getDb(), jsonChains);
        
        System.out.println("--- Ended ---");
	}

}
