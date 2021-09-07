package pt.tecnico.reg;

import io.grpc.stub.StreamObserver;
import pt.tecnico.reg.grpc.RecordServiceGrpc;
import static io.grpc.Status.INVALID_ARGUMENT;

import io.grpc.StatusRuntimeException;

import java.util.HashMap;

import pt.tecnico.reg.grpc.Reg;
import java.sql.Timestamp;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





public class RegImpl extends RecordServiceGrpc.RecordServiceImplBase{


   /*
	*	info:  hashmap that maps the name to a type and a value
	*/
	HashMap<String, HashMap<String, String>> info = new HashMap<String, HashMap<String, String>>();


	@Override
	public void ping(Reg.PingRequest request, StreamObserver<Reg.PingResponse> responseObserver) {
		String input = request.getInput();
		String output = input + " OK";
		/*
		Timestamp timestamp = o.getTimestamp();
		Long milliseconds = timestamp.getTime();
		com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder().setSeconds(milliseconds/1000).build();
		*/
		if (input == null || input.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty!").asRuntimeException());
		}

		Reg.PingResponse response = Reg.PingResponse.newBuilder().setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void read(Reg.ReadRequest request, StreamObserver<Reg.ReadResponse> responseObserver)  {
		String name = request.getName();

		if (name == null || name.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be empty!").asRuntimeException());
		}

		if (info.containsKey(name)) {

			if (info.get(name).containsKey(request.getType())) {
				Reg.ReadResponse response = Reg.ReadResponse.newBuilder().setRes(info.get(name).get(request.getType()))
						.build();
				responseObserver.onNext(response);
				responseObserver.onCompleted();
			} else{
				responseObserver.onError(INVALID_ARGUMENT
						.withDescription("ERRO Not possible convert the given type.").asRuntimeException());
			}
		} else {
			HashMap<String, String> map = new HashMap<String, String>();
			if(request.getType().equals("int") || request.getType().equals("age")){
				map.put(request.getType(), "0");
			}
			else {
				map.put(request.getType(), "");

			}

			info.put(name, map);

			Reg.ReadResponse response = null;
			if (request.getType().equals("int")) {
				response = Reg.ReadResponse.newBuilder().setRes("0").build();
			} else {
				response = Reg.ReadResponse.newBuilder().setRes("").build();
			}
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
}

	@Override
	public void write(Reg.WriteRequest request, StreamObserver<Reg.WriteResponse> responseObserver) {
		String name = request.getName();
		String type = request.getType();
		String value = request.getValue();

		if(type.equals("usr")) {
			if(name.length() < 3 || name.length() > 8 || value.length() < 5 || value.length() > 50) {
				responseObserver.onError(INVALID_ARGUMENT.withDescription("User ID or User name is not valid!").asRuntimeException());
				responseObserver.onCompleted();
			}
		}

		
	// Instructions to make sure type int: only receives numbers
		else if(type.equals("int")) {			
			if (Pattern.matches("[a-zA-Z]+[^0-9]*", value)) {
				responseObserver.onError(INVALID_ARGUMENT.withDescription("If the type is int please insert only numbers!").asRuntimeException());
				responseObserver.onCompleted();
			}
		}

	// Instructions to make sure type crd: only receives numbers
		else if(type.equals("crd")) {
			List<String> coords = new ArrayList<>(Arrays.asList(value.split(",")));
			if(coords.size() != 2) {
				responseObserver.onError(INVALID_ARGUMENT.withDescription("Must give 2 values separated by a \",\"!").asRuntimeException());
				responseObserver.onCompleted();
			}

			for(String c : coords){
				if (Pattern.matches("[a-zA-Z]+[^0-9]*", c)) {
					responseObserver.onError(INVALID_ARGUMENT.withDescription("Both values given must be floats!").asRuntimeException());
					responseObserver.onCompleted();
				}
			}
		}

	// Instructions to make sure type age: only receives names type - str and numbers type - int
		else if(type.equals("age")) {	
			if(Pattern.matches("[a-zA-Z]+", name) == false) {
				responseObserver.onError(INVALID_ARGUMENT.withDescription("Name must contain letters!").asRuntimeException());
				responseObserver.onCompleted();
			}
			if(Pattern.matches("[a-zA-Z]+[^0-9]*", value)) {
				responseObserver.onError(INVALID_ARGUMENT.withDescription("Value must be a number!").asRuntimeException());
				responseObserver.onCompleted();
			}
		}

		if (type == null || name == null || name.isBlank() || value == null) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Name cannot be empty!").asRuntimeException());
			responseObserver.onCompleted();
		}

		if (info.containsKey(name)) {
			if (info.get(name).containsKey(request.getType())) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(type, value);

				info.put(name, map);

			} else {
				responseObserver.onError(INVALID_ARGUMENT
						.withDescription("ERRO Not possible convert the given type.").asRuntimeException());
			}
		} else {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(type, value);

			info.put(name, map);
		}

		Reg.WriteResponse response = Reg.WriteResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

}