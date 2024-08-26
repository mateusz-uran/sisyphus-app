# python/Dockerfile

# Use the official Python image from the Docker Hub
FROM python:3.10.14-slim-bullseye

# Set environment variables to avoid Python buffering and to ensure that the output is sent straight to the terminal
ENV PYTHONUNBUFFERED=1

# Create and set the working directory
WORKDIR /scraper

# Copy the requirements file into the container
COPY requirements.txt /scraper/

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application code into the container
COPY . /scraper/

# Expose the port that the application will run on
EXPOSE 5858

# Set the environment variable for the frontend URL (optional default value can be overridden when running the container).
ENV FRONTEND_URL=http://localhost:9090

ENV PYTHONPATH=/scraper

# Specify the command to run your application
#CMD ["python", "app/api.py"]
CMD ["gunicorn", "-w", "4", "--bind", "0.0.0.0:5858", "app.api:app"]