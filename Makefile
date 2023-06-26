INPUTSVG:=svg/a-coffee-cup.svg
OUTPUTSVG:=a-coffee-cup.svg
EPSILON:=0.5

run: LineSimple.jar main.kts
	kotlinc -cp $< -script main.kts $(INPUTSVG) $(OUTPUTSVG) $(EPSILON)

LineSimple.jar: LineSimple.kt
	kotlinc $< -d $@

clean:
	$(RM) LineSimple.jar
	$(RM) a-coffee-cup.svg
