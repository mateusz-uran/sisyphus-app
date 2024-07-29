# python/Dockerfile

# Use the official Python image from the Docker Hub
FROM python:3.9-slim

# Set environment variables to avoid Python buffering and to ensure that the output is sent straight to the terminal
ENV PYTHONUNBUFFERED=1

# Create and set the working directory
WORKDIR /app

# Copy the requirements file into the container
COPY requirements.txt /app/

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application code into the container
COPY . /app/

# Expose the port that the application will run on
EXPOSE 5858

# Set the environment variable for the frontend URL
ENV FRONTEND_URL=http://localhost:9090

# Specify the command to run your application
CMD ["python", "app/api.py"]
