################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/DetectionBasedTracker_jni.cpp \
../jni/FeatureExtractor.cpp \
../jni/FeatureExtractor_jni.cpp 

OBJS += \
./jni/DetectionBasedTracker_jni.o \
./jni/FeatureExtractor.o \
./jni/FeatureExtractor_jni.o 

CPP_DEPS += \
./jni/DetectionBasedTracker_jni.d \
./jni/FeatureExtractor.d \
./jni/FeatureExtractor_jni.d 


# Each subdirectory must supply rules for building sources it contributes
jni/%.o: ../jni/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


