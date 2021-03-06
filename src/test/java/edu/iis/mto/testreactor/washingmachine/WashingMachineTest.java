package edu.iis.mto.testreactor.washingmachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

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
	private LaundryBatch overweightLaundryBatch;
	private Program nonAutoDetectProgram;
	private Material materialTypeJeans;
	private Material materialTypeCotton;
	private double anyProperWeightInKg;
	private double anyOverweightInKg;
	private ProgramConfiguration nonAutoDetectProgramConfigurationWithSpin;
	private ProgramConfiguration autodetectProgramConfigurationWithSpin;
	private LaundryStatus successWithNoErrors;
	private LaundryStatus failureTooHeavy;
	private static final Percentage AVERAGE_DEGREE = new Percentage(50.0d);
	private static final Percentage ABOVE_AVERAGE_DEGREE = new Percentage(50.1d);

	@BeforeEach
	void setUp() {
		washingMachine = new WashingMachine(dirtDetector, engine, waterPump);
		anyProperWeightInKg = 2d;
		materialTypeJeans = Material.JEANS;
		materialTypeCotton = Material.COTTON;
		nonAutoDetectProgram = Program.SHORT;
		properLaundryBatch = properLaundryBatchCreator();
		nonAutoDetectProgramConfigurationWithSpin = getNonAutoDetectProgramConfiguration(true);
		successWithNoErrors = statusSuccessWithNoErrors(nonAutoDetectProgram);
		anyOverweightInKg = 10d;
		overweightLaundryBatch = overweightLaundryBatchCreator();
		failureTooHeavy = statusFailureTooHeavy();
		autodetectProgramConfigurationWithSpin = getAutodetectProgramConfiguration(true);
	}

	//###TestyStanu###
	@Test
	void properBatchJeansTypeFiveKgShortProgramWithSpinExpectingSuccess() {
		LaundryStatus result = washingMachine.start(properLaundryBatch, nonAutoDetectProgramConfigurationWithSpin);

		assertEquals(successWithNoErrors, result);
	}

	@Test
	void overweightBatchCottonTypeTenKgShortProgramWithSpinExpectingStatusError() {
		LaundryStatus result = washingMachine.start(overweightLaundryBatch, nonAutoDetectProgramConfigurationWithSpin);

		assertEquals(failureTooHeavy, result);
	}

	@Test
	void properBatchJeansTypeFiveKgAutodetectAverageDirtDegreeWithSpinExpectingSuccess() {
		when(dirtDetector.detectDirtDegree(any())).thenReturn(AVERAGE_DEGREE);

		LaundryStatus result = washingMachine.start(properLaundryBatch, autodetectProgramConfigurationWithSpin);

		assertEquals(statusSuccessWithNoErrors(Program.MEDIUM), result);
	}

	@Test
	void properBatchJeansTypeFiveKgAutodetectAboveAverageDirtDegreeWithSpinExpectingSuccess() {
		when(dirtDetector.detectDirtDegree(any())).thenReturn(ABOVE_AVERAGE_DEGREE);

		LaundryStatus result = washingMachine.start(properLaundryBatch, autodetectProgramConfigurationWithSpin);

		assertEquals(statusSuccessWithNoErrors(Program.LONG), result);
	}

	@Test
	void properBatchJeansTypeFiveKgAutodetectAboveAverageDirtDegreeWithSpinExpectingPompFailure() throws WaterPumpException {
		when(dirtDetector.detectDirtDegree(any())).thenReturn(ABOVE_AVERAGE_DEGREE);
		doThrow(new WaterPumpException()).when(waterPump).pour(anyDouble());
		LaundryStatus result = washingMachine.start(properLaundryBatch, autodetectProgramConfigurationWithSpin);

		assertEquals(statusFailureWaterPumpFailure(Program.LONG), result);
	}

	@Test
	void properBatchJeansTypeFiveKgAutodetectAboveAverageDirtDegreeWithSpinExpectingEngineFailure() throws EngineException {
		when(dirtDetector.detectDirtDegree(any())).thenReturn(ABOVE_AVERAGE_DEGREE);
		doThrow(new EngineException()).when(engine).spin();
		LaundryStatus result = washingMachine.start(properLaundryBatch, autodetectProgramConfigurationWithSpin);

		assertEquals(statusFailureEngineFailure(Program.LONG), result);
	}

	@Test
	void wrongSetupExpectingUnknownError() {
		LaundryStatus result = washingMachine.start(properLaundryBatch, null);

		assertEquals(statusFailureUnknownError(null), result);
	}

	@Test
	void properBatchJeansTypeFiveKgAutodetectAboveAverageDirtDegreeWithSpinExpectingProperOrderOfOperations() throws WaterPumpException, EngineException {
		when(dirtDetector.detectDirtDegree(any())).thenReturn(ABOVE_AVERAGE_DEGREE);

		LaundryStatus result = washingMachine.start(properLaundryBatch, autodetectProgramConfigurationWithSpin);
		InOrder inOrder = Mockito.inOrder(dirtDetector, engine, waterPump);
		inOrder.verify(dirtDetector).detectDirtDegree(properLaundryBatch);
		inOrder.verify(waterPump).pour(anyDouble());
		inOrder.verify(engine).runWashing(anyInt());
		inOrder.verify(waterPump).release();
		inOrder.verify(engine).spin();

		assertEquals(statusSuccessWithNoErrors(Program.LONG), result);
	}

	private LaundryStatus statusSuccessWithNoErrors(Program programType) {
		return LaundryStatus.builder()
				.withResult(Result.SUCCESS)
				.withErrorCode(ErrorCode.NO_ERROR)
				.withRunnedProgram(programType)
				.build();
	}

	private LaundryStatus statusFailureTooHeavy() {
		return LaundryStatus.builder()
				.withResult(Result.FAILURE)
				.withErrorCode(ErrorCode.TOO_HEAVY)
				.withRunnedProgram(null)
				.build();
	}

	private LaundryStatus statusFailureWaterPumpFailure(Program p) {
		return LaundryStatus.builder()
				.withResult(Result.FAILURE)
				.withErrorCode(ErrorCode.WATER_PUMP_FAILURE)
				.withRunnedProgram(p)
				.build();
	}

	private LaundryStatus statusFailureUnknownError(Program p) {
		return LaundryStatus.builder()
				.withResult(Result.FAILURE)
				.withErrorCode(ErrorCode.UNKNOWN_ERROR)
				.withRunnedProgram(p)
				.build();
	}

	private LaundryStatus statusFailureEngineFailure(Program p) {
		return LaundryStatus.builder()
				.withResult(Result.FAILURE)
				.withErrorCode(ErrorCode.ENGINE_FAILURE)
				.withRunnedProgram(p)
				.build();
	}

	private ProgramConfiguration getNonAutoDetectProgramConfiguration(boolean spin) {
		return ProgramConfiguration.builder()
				.withProgram(nonAutoDetectProgram)
				.withSpin(spin)
				.build();
	}

	private ProgramConfiguration getAutodetectProgramConfiguration(boolean spin) {
		return ProgramConfiguration.builder()
				.withProgram(Program.AUTODETECT)
				.withSpin(spin)
				.build();
	}

	private LaundryBatch properLaundryBatchCreator() {
		return LaundryBatch.builder()
				.withMaterialType(materialTypeJeans)
				.withWeightKg(anyProperWeightInKg)
				.build();
	}

	private LaundryBatch overweightLaundryBatchCreator() {
		return LaundryBatch.builder()
				.withMaterialType(materialTypeCotton)
				.withWeightKg(anyOverweightInKg)
				.build();
	}

}
