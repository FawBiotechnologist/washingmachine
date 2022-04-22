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
    private WashingMachine washingMashine;

    @BeforeEach
    void setUp() throws Exception {
        washingMashine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    //###TestyStanu###
    @Test
    void properBatchJeansTypeFiveKgShortProgramWithSpinExpectingSuccess() {
        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withMaterialType(Material.JEANS)
                .withWeightKg(2)
                .build();
        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withProgram(Program.SHORT)
                .withSpin(true)
                .build();
        LaundryStatus result = washingMashine.start(laundryBatch, programConfiguration);
        LaundryStatus expectedResult = LaundryStatus.builder()
                .withResult(Result.SUCCESS)
                .withErrorCode(ErrorCode.NO_ERROR)
                .withRunnedProgram(Program.SHORT)
                .build();
        assertEquals(expectedResult, result);
    }

}
