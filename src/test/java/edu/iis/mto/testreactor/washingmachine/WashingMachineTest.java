package edu.iis.mto.testreactor.washingmachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WashingMachineTest {

    @Mock
    private DirtDetector dirtDetector;
    @Mock
    private Engine engine;
    @Mock
    private WaterPump waterPump;
    private WashingMachine washingMachine;
    private LaundryBatch properLaundryBatch;
    private Program nonAutoDetectProgram;
    private Material irrelevantMaterialType;
    private double anyProperWeightInKg;
    private ProgramConfiguration nonAutoDetectProgramConfigurationWithSpin;
    private LaundryStatus successWithNoErrors;

    @BeforeEach
    void setUp() throws Exception {
        washingMachine = new WashingMachine(dirtDetector, engine, waterPump);
        anyProperWeightInKg = 2d;
        irrelevantMaterialType = Material.JEANS;
        nonAutoDetectProgram = Program.SHORT;
        properLaundryBatch = properLaundryBatchCreator();
        nonAutoDetectProgramConfigurationWithSpin = getNonAutoDetectProgramConfiguration(true);
        successWithNoErrors = statusSuccessWithNoErrors(nonAutoDetectProgram);
    }

    //###TestyStanu###
    @Test
    void properBatchJeansTypeFiveKgShortProgramWithSpinExpectingSuccess() {
        LaundryStatus result = washingMachine.start(properLaundryBatch, nonAutoDetectProgramConfigurationWithSpin);

        assertEquals(successWithNoErrors, result);
    }

    private LaundryStatus statusSuccessWithNoErrors(Program programType) {
        return LaundryStatus.builder()
                .withResult(Result.SUCCESS)
                .withErrorCode(ErrorCode.NO_ERROR)
                .withRunnedProgram(programType)
                .build();
    }

    private ProgramConfiguration getNonAutoDetectProgramConfiguration(boolean spin) {
        return ProgramConfiguration.builder()
                .withProgram(nonAutoDetectProgram)
                .withSpin(spin)
                .build();
    }

    private LaundryBatch properLaundryBatchCreator() {
        return LaundryBatch.builder()
                .withMaterialType(irrelevantMaterialType)
                .withWeightKg(anyProperWeightInKg)
                .build();
    }

}
