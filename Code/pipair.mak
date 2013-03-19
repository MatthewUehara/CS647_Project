
$(TARGET)_3_65.out: $(TARGET).bc
	../pipair $< >$@ 2>&1

$(TARGET)_10_80.out: $(TARGET).bc
	../pipair $< 10 80 >$@ 2>&1

outputs: $(TARGET)_3_65.out $(TARGET)_10_80.out

